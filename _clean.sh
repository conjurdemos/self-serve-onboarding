#!/bin/bash
pushd java
  ./_clean.sh
popd
rm conjur-policy/*.yaml
