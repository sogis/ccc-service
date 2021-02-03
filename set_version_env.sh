#!/bin/bash

# Exports the full version to the env AGI_COMPONENT_VERSION

prefix='1.1.'

suffix=$GITHUB_BUILD_NUMBER
if [ -n suffix ]; then
  suffix='localbuild'
fi

export AGI_COMPONENT_VERSION=$prefix$suffix

echo 'Set env AGI_COMPONENT_VERSION to '$AGI_COMPONENT_VERSION
