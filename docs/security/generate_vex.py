#!/usr/bin/env python3
"""Generates docs/security/ccc-service.openvex.json (OpenVEX v0.2.0).

The VEX is bound to one image digest. After every image rebuild:
  1. scan:  grype sogis/ccc-service:latest
  2. update the component versions / statements below for new findings
  3. regenerate:
       python3 docs/security/generate_vex.py --digest <sha256:...> --version <N>
     (digest: docker image inspect sogis/ccc-service:latest --format '{{index .RepoDigests 0}}')
  4. verify:  grype --vex docs/security/ccc-service.openvex.json sogis/ccc-service:latest
"""
import argparse, json
from datetime import datetime, timezone
from pathlib import Path

ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
ap.add_argument("--digest", required=True, help="image digest, e.g. sha256:d4c6...")
ap.add_argument("--version", type=int, required=True, help="VEX document version (increment on every change)")
ap.add_argument("--output", default=str(Path(__file__).with_name("ccc-service.openvex.json")))
ap.add_argument("--app-version", default="", help="ccc-service build version / git commit, informational")
args = ap.parse_args()
DIGEST = args.digest.split("@")[-1]
REPO = "index.docker.io/sogis/ccc-service"
PROD = f"pkg:oci/ccc-service@{DIGEST.replace(':','%3A')}?repository_url={REPO}"
NOW = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
def mvn(g,a,v): return f"pkg:maven/{g}/{a}@{v}"
def apk(*names, v): return [f"pkg:apk/alpine/{n}@{v}" for n in names]
TC=[mvn("org.apache.tomcat.embed","tomcat-embed-core","10.1.61")]
SB=[mvn("org.springframework.boot","spring-boot","3.5.16")]
SBA=[mvn("org.springframework.boot","spring-boot-autoconfigure","3.5.16")]
SW=[mvn("org.springframework","spring-web","6.2.19")]
SMVC=[mvn("org.springframework","spring-webmvc","6.2.19")]
SEXP=[mvn("org.springframework","spring-expression","6.2.19")]
JK=[mvn("com.fasterxml.jackson.core","jackson-databind","2.21.7")]
MM=[mvn("io.micrometer","micrometer-core","1.15.12")]
L4=[mvn("org.apache.logging.log4j","log4j-api","2.26.1")]
LB=[mvn("ch.qos.logback","logback-core","1.5.34")]
OSSL=apk("openssl","libcrypto3","libssl3",v="3.5.9-r1")
CU=apk("coreutils","coreutils-env","coreutils-fmt","coreutils-sha512sum",v="9.11-r0")
BB=apk("busybox","busybox-binsh","ssl_client",v="1.37.0-r31")
S=[]
# Use the CVE id as name and list GHSA ids as aliases: Trivy matches only vulnerability.name,
# Grype matches name and aliases.
def st(cve, subs, status, aliases=(), **kw):
    vuln={"name":cve}
    if aliases: vuln["aliases"]=list(aliases)
    s={"vulnerability":vuln,"timestamp":NOW,
       "products":[{"@id":PROD,"subcomponents":[{"@id":x} for x in subs]}],"status":status}
    s.update(kw); S.append(s)
def fixed(cve, subs, note, aliases=()): st(cve, subs, "fixed", aliases, status_notes=note)
def na(cve, subs, impact, aliases=()): st(cve, subs, "not_affected", aliases, justification="vulnerable_code_not_in_execute_path", impact_statement=impact)

# --- Tomcat (pinned via ext['tomcat.version'])
P=" Image contains tomcat-embed-core 10.1.61 (pinned via ext['tomcat.version'] in build.gradle)."
fixed("CVE-2026-86350",TC,"Fixed in Tomcat 10.1.60."+P)
for c,d in [("CVE-2026-65905","DIGEST auth replay bypass"),("CVE-2026-65182","Security constraint bypass via declaration order"),("CVE-2026-68525","FORM auth bypass of POST-only constraint")]:
    fixed(c,TC,f"{d}; fixed upstream in 10.1.58 (first Maven Central release: 10.1.59)."+P)
for c in ("CVE-2026-43512","CVE-2026-41293","CVE-2026-43515","CVE-2026-34483","CVE-2026-34486","CVE-2026-34487"):
    fixed(c,TC,"Fixed in Tomcat 10.1.54/10.1.55."+P)
# --- Spring Boot
fixed("CVE-2026-40973",SB,"ApplicationTemp temp-dir vulnerability; fixed in Spring Boot 3.5.14. Image contains 3.5.16.")
fixed("CVE-2026-41001",SB+SBA,"Predictable temp/data directory in Artemis auto-configuration; fixed in Spring Boot 3.5.15."
      " Image contains 3.5.16 (Artemis is not on the classpath either).",aliases=["GHSA-ggg2-9786-hwc8"])
# --- Spring Framework 6.2.19 (pinned via ext['spring-framework.version'])
F=" Fixed in Spring Framework 6.2.19 (pinned via ext['spring-framework.version'])."
fixed("CVE-2026-41842",SMVC,"DoS via versioned static resources; static resource handling is auto-configured, so potentially reachable."+F)
fixed("CVE-2026-41841",SMVC,"Info disclosure via static resources; potentially reachable via auto-configured static resource handling."+F)
fixed("CVE-2026-41850",SEXP,"SpEL algorithmic DoS; the application evaluates no user-supplied SpEL."+F)
fixed("CVE-2026-41845",SW,"XSS via JavaScriptUtils.javaScriptEscape(); not used by the application."+F)
fixed("CVE-2026-41846",SMVC,"XSS via JSP form tags; the application serves no JSPs."+F)
SPRING_NOFIX = (" Fixed for 6.2.x only in 6.2.20 (Spring Enterprise Support only); open-source fix only in 7.0.9"
                " (Spring Boot 4). Grype/NVD lists CVSS 9.8; Spring rates it {}.")
na("CVE-2026-47884",SMVC,"XsltView SSRF/RCE. Requires an XsltView used for view rendering via a '/**' mapping"
   " without explicit view name. The application defines no XsltView, ViewResolver or view-returning controller and has no"
   " template engine; the only controller (StatusPage) is a @RestController returning text/plain. The only view Spring Boot"
   " renders is the whitelabel error page (a fixed, non-XSLT view); the default '/**' static-resource mapping serves"
   " resources, not views."+SPRING_NOFIX.format("MEDIUM (CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:C/C:L/I:N/A:N)"),
   aliases=["GHSA-pc63-qcmh-9cmg"])
na("CVE-2026-47890",SMVC,"SSE stream corruption with view fragments. Requires sending view fragments over"
   " Server-Sent Events with attacker-controlled data. The application uses no SSE (no SseEmitter, ServerSentEvent,"
   " text/event-stream), no view fragments and no WebFlux; messages are relayed over WebSocket only."
   +SPRING_NOFIX.format("LOW (CVSS:3.1/AV:N/AC:H/PR:L/UI:R/S:U/C:N/I:L/A:N)"),
   aliases=["GHSA-j9f9-w8pj-32f8"])
# --- Jackson, Micrometer, Log4j, Logback
fixed("CVE-2026-59889",JK,"@JsonView bypass; fixed in 2.21.6, image contains 2.21.7. The application uses neither @JsonView nor @JsonTypeInfo.",
      aliases=["GHSA-mhm7-754m-9p8w"])
fixed("CVE-2026-54513",JK,"Fixed in Jackson 2.21.4; image contains 2.21.7.")
fixed("CVE-2026-40984",MM,"HTTP server instrumentation DoS; reachable via actuator auto-instrumentation. Fixed in Micrometer 1.15.12 (pinned).")
na("CVE-2026-40983",MM,"gRPC server instrumentation DoS: no gRPC library is on the classpath and the application exposes no gRPC server. Micrometer is additionally at the fixed version 1.15.12.")
fixed("CVE-2026-49844",L4,"MapMessage.asJson() NaN/Infinity; fixed in 2.26.1 (pinned). log4j-api is only present transitively via log4j-to-slf4j; the application logs via SLF4J/Logback.")
fixed("CVE-2026-9828",LB,"Deserialization of untrusted data; fixed in logback 1.5.33. Image contains 1.5.34.",
      aliases=["GHSA-p47f-322f-whfh"])
fixed("CVE-2026-10532",LB,"Object injection through HardenedObjectInputStream; fixed in logback 1.5.34. Image contains 1.5.34.",
      aliases=["GHSA-jhq6-gfmj-v8fx"])
# --- Alpine base image (3.24.4): fixed
A=" Image (Alpine 3.24.4, 'apk upgrade --no-cache' in docker/Dockerfile) contains "
for c in ("CVE-2026-63073","CVE-2026-75803","CVE-2026-63076","CVE-2026-63072","CVE-2026-14456"):
    fixed(c,OSSL,"Fixed in openssl 3.5.8-r0."+A+"openssl/libcrypto3/libssl3 3.5.9-r1.")
fixed("CVE-2026-34182",OSSL,"CMS AuthEnvelopedData authentication bypass; fixed in openssl 3.5.7-r0 (Alpine secdb)."+A+"3.5.9-r1. The application does not use OpenSSL CMS in any case (JVM uses JSSE).")
for c,fx in (("CVE-2026-66046","2.8.4-r0"),("CVE-2026-76641","2.8.4-r0"),("CVE-2026-115506","2.10.0-r0")):
    fixed(c,apk("libexpat",v="2.10.0-r0"),f"Fixed in expat {fx}."+A+"libexpat 2.10.0-r0.")
fixed("CVE-2026-4738",apk("zlib",v="1.4-r0"),"Fixed in zlib 1.4-r0."+A+"zlib 1.4-r0.")
fixed("CVE-2026-111497",apk("brotli-libs",v="1.2.1-r0"),"Fixed in brotli 1.2.1-r0."+A+"brotli-libs 1.2.1-r0.")
fixed("CVE-2026-25646",apk("libpng",v="1.6.59-r0"),"Fixed in libpng 1.6.55-r0."+A+"libpng 1.6.59-r0.")
fixed("CVE-2026-40930",apk("libpng",v="1.6.59-r0"),"Fixed in libpng 1.6.58-r1 (Alpine 3.24)."+A+"libpng 1.6.59-r0.")
fixed("CVE-2026-23865",apk("freetype",v="2.14.3-r0"),"Originally reported against freetype 2.14.1-r0. The Alpine secdb lists no affected freetype version for Alpine 3.24, and Grype no longer reports it; the image contains freetype 2.14.3-r0. (The JRE is Eclipse Temurin, not the Alpine openjdk25 package that the secdb also lists.)")
# --- Alpine base image: not affected (no fix available)
na("CVE-2025-60876",BB,"Request-line injection in BusyBox wget: wget is never invoked. The only busybox use at runtime is ash parsing the shell-form CMD once at startup before 'exec java' replaces it; ash is not the vulnerable component.")
na("CVE-2016-2781",CU,"chroot --userspec TIOCSTI escape: chroot is never invoked; the container runs only the JVM as non-root uid 1001.")
na("CVE-2026-56392",CU,"Heap overflow in coreutils unexpand: unexpand is never invoked; the container runs only the JVM.")
na("CVE-2026-56391",CU,"Out-of-bounds read in coreutils uniq -w: uniq is never invoked; the container runs only the JVM.")
O="System OpenSSL (libcrypto3/libssl3) is not used by the application: TLS is handled by the JVM's own stack (JSSE) and the service uses no "
for c,what in (("CVE-2026-124709","QUIC"),("CVE-2026-107494","QUIC"),("CVE-2026-95690","DTLS"),("CVE-2026-84786","DANE"),("CVE-2026-35190","OpenSSL TLS server with Suite B flags"),("CVE-2026-121482","CMS/PKCS#7 verification"),("CVE-2026-107493","PEM_X509_INFO_read_bio_ex()")):
    na(c,OSSL,O+what+". No fixed Alpine package available yet.")
doc={"@context":"https://openvex.dev/ns/v0.2.0",
 "@id":"https://github.com/sogis/ccc-service/blob/master/docs/security/ccc-service.openvex.json",
 "author":"sogis","role":"Product maintainer",
 "timestamp":"2026-11-24T00:00:00Z","last_updated":NOW,"version":args.version,
 "tooling":"docs/security/generate_vex.py; verified with Grype against sogis/ccc-service@"+DIGEST
           +(f" ({args.app_version})" if args.app_version else ""),
 "statements":S}
with open(args.output, "w", encoding="utf-8") as f:
    json.dump(doc, f, indent=2, ensure_ascii=False)
    f.write("\n")
print(f"{len(S)} statements -> {args.output}")
