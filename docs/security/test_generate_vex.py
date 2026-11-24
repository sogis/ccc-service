"""Tests for generate_vex.py.

Run: python3 -m pytest docs/security/test_generate_vex.py
"""
import json
import re
import subprocess
import sys
from pathlib import Path

import pytest

HERE = Path(__file__).parent
SCRIPT = HERE / "generate_vex.py"
COMMITTED = HERE / "ccc-service.openvex.json"
DIGEST = "sha256:" + "ab" * 32

# OpenVEX v0.2.0 vocabulary
STATUSES = {"not_affected", "affected", "fixed", "under_investigation"}
JUSTIFICATIONS = {
    "component_not_present",
    "vulnerable_code_not_present",
    "vulnerable_code_not_in_execute_path",
    "vulnerable_code_cannot_be_controlled_by_adversary",
    "inline_mitigations_already_exist",
}
TS = re.compile(r"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z$")
PURL = re.compile(r"^pkg:(maven|apk)/[^@]+/[^@/]+@[^@]+$")


def run(*args):
    return subprocess.run([sys.executable, "-I", str(SCRIPT), *args],
                          capture_output=True, text=True)


def generate(tmp_path, digest=DIGEST, version="7", extra=()):
    out = tmp_path / "vex.json"
    r = run("--digest", digest, "--version", version, "--output", str(out), *extra)
    assert r.returncode == 0, r.stderr
    return json.loads(out.read_text(encoding="utf-8"))


@pytest.fixture
def doc(tmp_path):
    return generate(tmp_path, extra=("--app-version", "ccc-service 9.9.9"))


# --- CLI ---------------------------------------------------------------------

@pytest.mark.parametrize("args", [[], ["--digest", DIGEST], ["--version", "1"],
                                  ["--digest", DIGEST, "--version", "x"]])
def test_missing_or_invalid_args_fail(args):
    r = run(*args)
    assert r.returncode == 2
    assert "usage" in r.stderr


def test_output_reports_statement_count(tmp_path):
    out = tmp_path / "vex.json"
    r = run("--digest", DIGEST, "--version", "1", "--output", str(out))
    assert re.search(r"^\d+ statements -> ", r.stdout)


# --- document metadata ---------------------------------------------------------

def test_document_metadata(doc):
    assert doc["@context"] == "https://openvex.dev/ns/v0.2.0"
    assert doc["@id"].startswith("https://")
    assert doc["author"]
    assert doc["version"] == 7
    assert TS.match(doc["timestamp"]) and TS.match(doc["last_updated"])
    assert doc["last_updated"] >= doc["timestamp"]
    assert DIGEST in doc["tooling"] and "ccc-service 9.9.9" in doc["tooling"]


@pytest.mark.parametrize("given", [DIGEST, "sogis/ccc-service@" + DIGEST])
def test_product_is_bound_to_digest(tmp_path, given):
    d = generate(tmp_path, digest=given)
    expected = ("pkg:oci/ccc-service@" + DIGEST.replace(":", "%3A")
                + "?repository_url=index.docker.io/sogis/ccc-service")
    ids = {p["@id"] for s in d["statements"] for p in s["products"]}
    assert ids == {expected}


# --- statements ----------------------------------------------------------------

def test_statements_are_unique_and_valid(doc):
    names = [s["vulnerability"]["name"] for s in doc["statements"]]
    assert names, "no statements"
    assert len(names) == len(set(names)), "duplicate vulnerability"
    for s in doc["statements"]:
        n = s["vulnerability"]["name"]
        assert s["status"] in STATUSES, n
        assert TS.match(s["timestamp"]), n


def test_names_are_cves_and_ghsas_are_aliases(doc):
    """Trivy matches only vulnerability.name (it reports CVE ids); Grype also matches aliases.
    An alias must not reappear as another statement, otherwise the same vulnerability is
    assessed twice."""
    names = {s["vulnerability"]["name"] for s in doc["statements"]}
    all_ids = []
    for s in doc["statements"]:
        v = s["vulnerability"]
        assert re.match(r"^CVE-\d{4}-\d{4,}$", v["name"]), v["name"]
        for a in v.get("aliases", []):
            assert re.match(r"^GHSA(-[a-z0-9]{4}){3}$", a), a
            assert a not in names, a
        all_ids += [v["name"], *v.get("aliases", [])]
    assert len(all_ids) == len(set(all_ids)), "vulnerability id used twice"


def test_status_specific_fields(doc):
    for s in doc["statements"]:
        n = s["vulnerability"]["name"]
        if s["status"] == "not_affected":
            # OpenVEX: justification or impact_statement required; we require both
            assert s.get("justification") in JUSTIFICATIONS, n
            assert len(s.get("impact_statement", "")) > 20, n
        else:
            assert "justification" not in s and "impact_statement" not in s, n
        if s["status"] == "fixed":
            assert s.get("status_notes"), n
        if s["status"] == "affected":
            assert s.get("action_statement"), n


def test_subcomponents_are_versioned_purls(doc):
    for s in doc["statements"]:
        subs = [c["@id"] for p in s["products"] for c in p.get("subcomponents", [])]
        assert subs, s["vulnerability"]["name"]
        for purl in subs:
            assert PURL.match(purl), purl


def test_alpine_binary_packages_are_listed(doc):
    """Grype matches Alpine binary packages (libcrypto3, ...), not only the origin package."""
    by_name = {s["vulnerability"]["name"]: s for s in doc["statements"]}
    expected = {"CVE-2026-124709": ["openssl", "libcrypto3", "libssl3"],
                "CVE-2016-2781": ["coreutils", "coreutils-env", "coreutils-fmt", "coreutils-sha512sum"],
                "CVE-2025-60876": ["busybox", "busybox-binsh", "ssl_client"]}
    for cve, pkgs in expected.items():
        subs = {c["@id"].split("/")[2].split("@")[0]
                for p in by_name[cve]["products"] for c in p["subcomponents"]}
        assert set(pkgs) <= subs, cve


# --- committed file in sync with generator ---------------------------------------

def _normalize(d):
    d = json.loads(json.dumps(d))
    for k in ("last_updated", "version", "tooling"):
        d.pop(k, None)
    for s in d["statements"]:
        s.pop("timestamp", None)
        for p in s["products"]:
            p["@id"] = re.sub(r"@sha256%3A[0-9a-f]+", "@<digest>", p["@id"])
    return d


def test_committed_vex_matches_generator(tmp_path):
    committed = json.loads(COMMITTED.read_text(encoding="utf-8"))
    generated = generate(tmp_path)
    assert _normalize(committed) == _normalize(generated), \
        "ccc-service.openvex.json is out of date - regenerate it with generate_vex.py"
