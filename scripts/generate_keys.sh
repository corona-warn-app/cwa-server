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

# Self-sign a certificate for EFGS batch signing
# $1 = IN  Private key
# $2 = IN  Certificate Header Parameters
# $3 = OUT New certificate file
self_sign_efgs_signing_certificate()
{
  openssl req -x509 -new \
    -days 365 \
    -key "$1" \
    -extensions v3_req \
    -subj "$2" \
    -nodes \
    -out "$3"
}

# Generate Certificate SHA256 thumbprint
# $1 = IN  X509 Certificate File
# $2 = OUT Thumbprint File
generate_certificate_thumbprint()
{
  openssl x509 -in "$1" -noout -hash -sha256 -fingerprint \
    | grep Fingerprint | sed 's/SHA256 Fingerprint=//' | sed 's/://g' >> "$2"
}


generate_private_key private.pem
extract_public_key private.pem public.pem
generate_certificate_signing_request private.pem '/CN=CWA Test Certificate' request.csr
self_sign_certificate_request request.csr private.pem certificate.crt

generate_private_key efgs_signing_key.pem
self_sign_efgs_signing_certificate efgs_signing_key.pem '/CN=CWA Test Certificate/OU=CWA-Team/C=DE' efgs_signing_cert.pem
generate_certificate_thumbprint efgs_signing_cert.pem efgs_x509_thumbprint.txt

popd > /dev/null || exit
popd > /dev/null || exit
