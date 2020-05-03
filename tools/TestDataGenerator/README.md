# ENA Test Data Generator
This CLI can generate and verify test data that adheres to the file exchange protocol defined in `/spec/file-xchange.proto`.
Signatures are calculated using Ed25519 elliptic curve.
## Build
`mvn package`
## Run
`java -jar TestDataGenerator.jar`
## Help
```
Usage: testDataGenerator [-hV] (generate | verify)
Generates and verifies example exposure keys.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  generate  Generate test data
  verify    Verify test data
```
### Generate command
```
Usage: testDataGenerator generate [-hV] [--force_empty]
                                  --avg_exposures_per_hour=<exposures_per_hour>
                                  --certificate=<certificate_file>
                                  --hours=<hours> --out=<out_directory>
                                  --private_key=<private_key_file>
Generate test data.
      --avg_exposures_per_hour=<exposures_per_hour>
                        Number of average exposure keys to generate per hour(or
                          avg_exposures_per_hour * 24 per day respectively).
                        Exposures per hour are randomized by poisson
                          distribution withepsilon = avg_exposures_per_hour
                          (rounded to the nearest value).
      --certificate=<certificate_file>
                        The Ed25519 certificate chain to attach to the payload.
                        Supported Format: X.509
      --force_empty     This will force the generation of at least one hourly
                          file that will not contain anyexposure keys.
                        Requires '--hours' to be set to a value n for which n
                          mod 24 >= 1.
  -h, --help            Show this help message and exit.
      --hours=<hours>   Number of hours for which to generate exposure keys.

                        A daily digest file will be generated for every 24h
                          chunk into the 'days' directory.
                        Leftover hours (n mod 24) will be generated into the
                          'hours' directory.

                        Example: A value of 181 will result in the generation
                          of 7 day files and 13 hour files(20 files total).
      --out=<out_directory>
                        Target directory (will be wiped before data generation).
                        Will contain a 'days' and an 'hours' directory.
      --private_key=<private_key_file>
                        The Ed25519 private key that shall be used for signing.
                        Supported format: PEM PKCS1/PKCS8
  -V, --version         Print version information and exit.

Process finished with exit code 0

```
### Verify command
```
Usage: testDataGenerator verify [-hV] --certificate=<certificate_file>
                                --in=<in_directory>
Verify test data
      --certificate=<certificate_file>
                            The Ed25519 certificate chain to use for
                              verification. Supported Format: X.509
  -h, --help                Show this help message and exit.
      --in=<in_directory>   Directory of files to be verified.
  -V, --version             Print version information and exit.
```
## Examples
### Generate test data
`java -jar TestDataGenerator.jar generate --hours 181 --avg_exposures_per_hour 300 --force_empty --out=./out --private_key=certificates/client/private.pem --certificate=certificates/chain/certificate.crt`
### Verify test data
`java -jar TestDataGenerator.jar verify --in=./out --certificate=certificates/chain/certificate.crt`
