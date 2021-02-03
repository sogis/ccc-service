#!/bin/bash

# Concats the full version and prints it to the stdout

prefix='1.1.'

suffix=$GITHUB_BUILD_NUMBER
if [ -n suffix ]; then
  suffix='localbuild'
fi

echo $prefix$suffix