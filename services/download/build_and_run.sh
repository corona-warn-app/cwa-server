#!/bin/bash

# Builds and runs the distribution module

pushd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null || exit

cd ../..

docker build                                              \
  --rm                                                    \
  -f services/download/Dockerfile                     \
  -t download . &&                                    \
docker run                                                \
  -it download

popd > /dev/null || exit
