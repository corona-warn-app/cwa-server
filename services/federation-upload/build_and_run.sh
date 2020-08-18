#!/bin/bash

# Builds and runs the federation-upload module

pushd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null || exit

cd ../..

docker build                                              \
  --rm                                                    \
  -f services/federation-upload/Dockerfile                     \
  -t federation-upload . &&                                    \
docker run                                                \
  -it federation-upload

popd > /dev/null || exit
