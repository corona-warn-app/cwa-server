#!/bin/bash

# Builds and runs the distribution module

pushd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null || exit

cd ../..

docker build                                              \
  --rm                                                    \
  -f services/federationdownload/Dockerfile                     \
  -t federationdownload . &&                                    \
docker run                                                \
  -it federationdownload

popd > /dev/null || exit
