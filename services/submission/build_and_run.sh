#!/bin/bash

# Builds and runs the submission module

pushd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null || exit

cd ../..

docker build                                              \
  --rm                                                    \
  -f services/submission/Dockerfile                       \
  -t submission . &&                                      \
docker run                                                \
  -p 127.0.0.1:8080:8080/tcp                              \
  -it submission

popd > /dev/null || exit