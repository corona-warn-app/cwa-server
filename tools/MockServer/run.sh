#!/bin/bash

# Builds and runs the Dockerfile in /tools/MockServer

pushd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null || exit

docker build                                              \
  -f Dockerfile                                           \
  -t ena-server-mock ../ &&                               \
docker run                                                \
  -p 127.0.0.1:8080:8080/tcp                              \
  -it ena-server-mock

popd > /dev/null || exit