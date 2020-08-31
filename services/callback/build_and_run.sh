#!/bin/bash

# Builds and runs the callback module

pushd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null || exit

cd ../..

docker build                                              \
  --rm                                                    \
  -f services/callback/Dockerfile                       \
  -t callback . &&                                      \
docker run                                                \
  -p 127.0.0.1:8080:8080/tcp                              \
  -it callback

popd > /dev/null || exit
