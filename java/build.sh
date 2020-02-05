#!/bin/bash
BUILD_DIRS="javarest pas dap ."
for i in $BUILD_DIRS; do
  pushd $i; ./0-compile.sh; popd
done
