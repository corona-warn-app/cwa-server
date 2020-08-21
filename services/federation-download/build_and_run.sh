#!/bin/bash

# Builds and runs the distribution module

pushd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null || exit

cd ../..

docker build                                              \
  --rm                                                    \
  -f services/federation-download/Dockerfile                     \
  -t federation-download . &&                                    \
docker run                                                \
  -it federation-download

popd > /dev/null || exit
