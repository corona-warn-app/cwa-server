#!/bin/bash

# Builds and runs the upload module

pushd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null || exit

cd ../..

docker build                                              \
  --rm                                                    \
  -f services/upload/Dockerfile                     \
  -t upload . &&                                    \
docker run                                                \
  -it upload

popd > /dev/null || exit
