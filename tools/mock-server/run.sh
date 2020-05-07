#!/bin/bash

# Builds and runs the Dockerfile in /tools/mock-server

pushd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null || exit

docker build                                              \
  -f Dockerfile                                           \
  -t cwa-server-mock ../ &&                               \
docker run                                                \
  -p 127.0.0.1:8080:8080/tcp                              \
  -it cwa-server-mock

popd > /dev/null || exit