#!/bin/bash

# Generates a prime256v1 EC private key and extracts the respective public key.
# This script requires openssl to be installed, see here:
## https://www.openssl.org/source/

pushd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null || exit

rm -rf keys
mkdir keys
pushd keys > /dev/null || exit

# Generate a prime256v1 EC private key
# $1 = OUT Private key file
generate_private_key()
{
  openssl ecparam                             \
    -name prime256v1                          \
    -genkey                                   \
    -out "$1"                                 \
    -noout
}

# Extract the public key from the private key
# $1 = IN  Private key
# $2 = OUT Public key
extract_public_key()
{
  openssl ec                                  \
    -in "$1"                                  \
    -pubout                                   \
    -out "$2"
}

generate_private_key private.pem
extract_public_key private.pem public.pem

popd > /dev/null || exit
popd > /dev/null || exit