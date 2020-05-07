#!/bin/bash

# Generates Java classes from all proto files found in "/spec" and puts them in the appropriate
# src folder. This script requires protoc to be installed, see here:
# https://developers.google.com/protocol-buffers/docs/downloads

pushd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null || exit

cd ../common/protocols/src/main/proto || exit
protoc --java_out=../java --proto_path=. *.proto

popd > /dev/null || exit