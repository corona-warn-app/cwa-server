# CWA Test Data Generator
This CLI can generate and verify test data that adheres to the file exchange protocol defined in `/spec/file-xchange.proto`.
Signatures are calculated using Ed25519 elliptic curve.
## Build
`mvn package`
## Run
`java -jar TestDataGenerator.jar`
## Help
```
Usage: testDataGenerator [-hV] (generate)
Generates example exposure keys.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  generate  Generate test data
```
### Generate command
```
Usage: testDataGenerator generate [-hV]
                                  --avg_exposures_per_hour=<exposures_per_hour>
                                  --certificate=<certificate_file>
                                  --hours=<hours> [--openapi=<openapi>]
                                  --out=<out_directory>
                                  --private_key=<private_key_file>
                                  [--seed=<seed>] --start_date=<start_date>
Generate test data according to the API spec.
      --avg_exposures_per_hour=<exposures_per_hour>
                            Number of average exposure keys to generate per hour
                              (or avg_exposures_per_hour * 24 per day
                              respectively).
                            Exposures per hour are randomized by poisson
                              distribution withepsilon = avg_exposures_per_hour
                              (rounded to the nearest value).

                            Example: 300
      --certificate=<certificate_file>
                            The Ed25519 certificate chain to attach to the
                              payload.
                            Supported Format: X.509

                            Example: ./certificates/chain/certificate.crt
  -h, --help                Show this help message and exit.
      --hours=<hours>       Number of hours for which to generate exposure
                              keys, starting at 00:00 on the date defined by
                              --start_date. A daily digest file will be
                              generated for every complete 24h chunk. Leftover
                              hours (n mod 24) will be generated into the
                              'hour' directory of the last day, but no daily
                              digest file will be generated.

                            Example: A value of 181 will result in the
                              generation of 181 hourly digest files (24 for
                              each of the seven days plus 13 leftover for the
                              last day) and 7 daily digest files (188 files
                              total).
      --openapi=<openapi>   Optional: An OpenAPI definition file to include.

                            Example: ../../services/distribution/api_v1.json
      --out=<out_directory> Target directory (will be wiped before data
                              generation).
                            Will contain directories that match the REST API
                              definition of the Distribution service.

                            Example: ./out
      --private_key=<private_key_file>
                            The Ed25519 private key that shall be used for
                              signing.
                            Supported format: PEM PKCS1/PKCS8

                            Example: ./certificates/client/private.pem
      --seed=<seed>         A seed for random data generation.

                            Example: 123456

                            Default: 123456
      --start_date=<start_date>
                            Date on which to start generating hourly and daily
                              files. Generation will start at 00:00 on the
                              start date and proceed forwards in time from
                              there on. Format: ISO-8601

                            Example: 2020-05-01
  -V, --version             Print version information and exit.
```
## Examples
### Generate test data
`java -jar TestDataGenerator.jar generate --hours 330 --start_date 2020-04-27 --avg_exposures_per_hour 1000 --openapi ../../services/distribution/api_v1.json --out=./out --private_key=./certificates/client/private.pem --certificate=./certificates/chain/certificate.crt --seed 123456`
