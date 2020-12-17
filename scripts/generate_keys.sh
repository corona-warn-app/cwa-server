#!/bin/bash

# Generates a prime256v1 EC private key and extracts the respective public key.
# This script requires openssl to be installed, see here:
## https://www.openssl.org/source/

pushd "$(dirname "${BASH_SOURCE[0]}")" > /dev/null || exit

echo "########### rm keys dir"
rm -rf keys
mkdir keys
pushd keys > /dev/null || exit

# Generate a prime256v1 EC private key
# $1 = OUT Private key file
generate_private_key()
{
  echo "########### generate_private_key"
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
  echo "########### extract_public_key from $1"
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
  echo "########### generate_certificate_signing_request for $1 with $2"
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
  echo "########### self_sign_certificate_request for $1 with $2"
  openssl x509                                \
    -req                                      \
    -days 365                                 \
    -in "$1"                                  \
    -signkey "$2"                             \
    -extfile ../domainnames.conf              \
    -out "$3"
}

# Self-sign a certificate for EFGS batch signing
# $1 = IN  Private key
# $2 = IN  Certificate Header Parameters
# $3 = OUT New certificate file
self_sign_efgs_signing_certificate()
{
  echo "########### self_sign_efgs_signing_certificate for $1 with $2"
  openssl req -x509 -new \
    -days 365 \
    -key "$1" \
    -extensions v3_req \
    -subj "$2" \
    -nodes \
    -out "$3"
}

# Generate a SSL keystore
# $1 = IN  Private key
# $2 = IN  Certificate
# $3 = OUT New pkcs12 keystore containing the certificate and signed with the private key
generate_SSL_keystore()
{
  echo "########### generate_SSL_keystore for $1 with $2"
  openssl pkcs12 -export \
    -in "$2" \
    -inkey "$1" \
    -passout pass:123456 \
    -out "$3"
}

# Generate Certificate SHA256 thumbprint
# $1 = IN  X509 Certificate File
# $2 = OUT Thumbprint File
generate_certificate_thumbprint()
{
  echo "########### generate_certificate_thumbprint for $1"
  openssl x509 -in "$1" -noout -hash -sha256 -fingerprint \
    | grep Fingerprint | sed 's/SHA256 Fingerprint=//' | sed 's/://g' >> "$2"
}

generate_truststore_containing_efgs()
{
  echo "########### generate_truststore_containing_efgs for $1 with alias $2"
  keytool -import \
	  -file "$1" \
	  -alias "$2" \
	  -storetype JKS \
	  -storepass 123456 \
	  -noprompt \
	  -ext "SAN:c=DNS:localhost,IP:127.0.0.1" \
	  -keystore "$3"
}

# Generate everything on CWA side
#generate_private_key private.pem
cp ../../docker-compose-test-secrets/private.pem . 
extract_public_key private.pem public.pem
generate_certificate_signing_request private.pem '/CN=localhost/OU=CWA-Backend-Team' request.csr
self_sign_certificate_request request.csr private.pem certificate.crt
generate_SSL_keystore private.pem certificate.crt ssl.p12

# Generate everything on EFGS side
#generate_private_key efgs_signing_key.pem
cp ../../docker-compose-test-secrets/efgs_signing_key.pem . 
self_sign_efgs_signing_certificate efgs_signing_key.pem '/CN=localhost' efgs_signing_cert.pem
generate_certificate_thumbprint efgs_signing_cert.pem efgs_x509_thumbprint.txt
generate_certificate_signing_request efgs_signing_key.pem '/CN=localhost' request.csr
self_sign_certificate_request request.csr efgs_signing_key.pem efgs.crt
generate_SSL_keystore efgs_signing_key.pem efgs.crt efgs.p12
generate_truststore_containing_efgs efgs.crt 'efgs' contains_efgs_truststore.jks

popd > /dev/null || exit
popd > /dev/null || exit
