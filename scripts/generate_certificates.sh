#!/bin/bash

# Generates a new self-signed X.509 root certificate and a client certificate. Only to be used for
# testing purposes. This script requires openssl to be installed, see here:
## https://www.openssl.org/source/

pushd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null || exit

rm -rf certificates
mkdir certificates
pushd certificates > /dev/null || exit

# Generate a private key
# $1 = OUT Private key file
generate_private_key()
{
  openssl genpkey                             \
    -algorithm ED25519                        \
    -out "$1"
}

# Generate certificate signing request
# $1 = IN  New certificate private key
# $2 = IN  "Subject" of the certificate
# $3 = OUT Certificate signing request file
generate_certificate_signing_request()
{
  openssl req                                 \
    -new                                      \
    -key "$1"                                 \
    -subj "$2"                                \
    -out "$3"
}

# Self-sign a certificate request
# $1 = IN  Certificate signing request file
# $2 = IN  Certificate authority private key file
# $3 = OUT New certificate file
self_sign_certificate_request()
{
  openssl x509                                \
    -req                                      \
    -days 365                                 \
    -in "$1"                                  \
    -signkey "$2"                             \
    -out "$3"
}

# Sign a certificate request using a CA certificate + private key
# $1 = IN  Certificate signing request file
# $2 = IN  Certificate authority certificate file
# $3 = IN  Certificate authority private key file
# $4 = OUT New certificate file
ca_sign_certificate_request()
{
  openssl x509 \
    -req \
    -days 365                                 \
    -set_serial 100 \
    -in "$1" \
    -CA "$2" \
    -CAkey "$3" \
    -out "$4"
}

# Self-signed root certificate
mkdir root
generate_private_key root/private.pem
generate_certificate_signing_request root/private.pem '/CN=ENA Test Root Certificate' root/request.csr
self_sign_certificate_request root/request.csr root/private.pem root/certificate.crt

# Client certificate signed by root certificate
mkdir client
generate_private_key client/private.pem
generate_certificate_signing_request client/private.pem '/CN=ENA Test Client Certificate' client/request.csr
ca_sign_certificate_request client/request.csr root/certificate.crt root/private.pem client/certificate.crt

# Concatenate the root certifricate and the client certificate.
# This way, we have the whole certificate chain in a single file.
mkdir chain
cat client/certificate.crt root/certificate.crt >> chain/certificate.crt

# Final verification of the certificate chain
openssl verify -CAfile root/certificate.crt chain/certificate.crt

popd > /dev/null || exit
popd > /dev/null || exit