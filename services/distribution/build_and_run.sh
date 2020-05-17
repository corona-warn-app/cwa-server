#!/bin/bash

# Builds and runs the distribution module

pushd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null || exit

cd ../..

docker build                                              \
  --rm                                                    \
  -f services/distribution/Dockerfile                     \
  -t distribution . &&                                    \
docker run                                                \
  -it distribution

popd > /dev/null || exit