<!-- markdownlint-disable MD041 -->
<h1 align="center">
    Corona-Warn-App Server
</h1>

<p align="center">
    <a href="https://github.com/corona-warn-app/cwa-server/commits/" title="Last Commit"><img src="https://img.shields.io/github/last-commit/corona-warn-app/cwa-server?style=flat"></a>
    <a href="https://github.com/corona-warn-app/cwa-server/issues" title="Open Issues"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-server?style=flat"></a>
    <a href="https://circleci.com/gh/corona-warn-app/cwa-server" title="Build Status"><img src="https://circleci.com/gh/corona-warn-app/cwa-server.svg?style=shield&circle-token=4ab059989d10709df19eb4b98ab7c121a25e981a"></a>
        <a href="https://sonarcloud.io/dashboard?id=corona-warn-app_cwa-server" title="Quality Gate"><img src="https://sonarcloud.io/api/project_badges/measure?project=corona-warn-app_cwa-server&metric=alert_status"></a>
        <a href="https://sonarcloud.io/component_measures?id=corona-warn-app_cwa-server&metric=Coverage&view=list" title="Coverage"><img src="https://sonarcloud.io/api/project_badges/measure?project=corona-warn-app_cwa-server&metric=coverage"></a>
    <a href="https://github.com/corona-warn-app/cwa-server/blob/master/LICENSE" title="License"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg?style=flat"></a>
</p>

<p align="center">
  <a href="#development">Development</a> •
  <a href="#service-apis">Service APIs</a> •
  <a href="#documentation">Documentation</a> •
  <a href="#support-and-feedback">Support</a> •
  <a href="#how-to-contribute">Contribute</a> •
  <a href="#contributors">Contributors</a> •
  <a href="#repositories">Repositories</a> •
  <a href="#licensing">Licensing</a>
</p>

The goal of this project is to develop the official Corona-Warn-App for Germany based on the exposure notification API from [Apple](https://www.apple.com/covid19/contacttracing/) and [Google](https://www.google.com/covid19/exposurenotifications/). The apps (for both iOS and Android) use Bluetooth technology to exchange anonymous encrypted data with other mobile phones (on which the app is also installed) in the vicinity of an app user's phone. The data is stored locally on each user's device, preventing authorities or other parties from accessing or controlling the data. This repository contains the **implementation of the server for encryption keys** for the Corona-Warn-App.

In this documentation, Corona-Warn-App services are also referred to as CWA services.

## Architecture Overview

You can find the architecture overview [here](/docs/ARCHITECTURE.md), which will give you
a good starting point in how the backend services interact with other services, and what purpose
they serve.

## Development

After you've checked out this repository, you can run the application in one of the following ways:

* As a [Docker](https://www.docker.com/)-based deployment on your local machine. You can run either:
  * Single components using the respective Dockerfile or
  * The full backend using the Docker Compose (which is considered the most convenient way)
* As a [Maven](https://maven.apache.org)-based build on your local machine.
  If you want to develop something in a single component, this approach is preferable.

### Docker-Based Deployment

If you want to use Docker-based deployment, you need to install Docker on your local machine. For more information about downloading and installing Docker, see the [official Docker documentation](https://docs.docker.com/get-docker/).

#### Running the Full CWA Backend Using Docker Compose

For your convenience, a full setup for local development and testing purposes, including the generation of test data has been prepared using [Docker Compose](https://docs.docker.com/compose/reference/overview/). To build the backend services, run ```docker-compose build``` in the repository's root directory. A default configuration file can be found under ```.env``` in the root folder of the repository. If the endpoints are to be exposed to the network the default values in this file should be changed before docker-compose is run.

Once the services are built, you can start the whole backend using ```docker-compose up```.
The distribution service runs once and then finishes. If you want to trigger additional distribution runs, run ```docker-compose run distribution```.

The docker-compose contains the following services:

Service           | Description | Endpoint and Default Credentials
------------------|-------------|-----------
submission        | The Corona-Warn-App submission service                                                      | `http://localhost:8000` <br> `http://localhost:8006` (for actuator endpoint)
distribution      | The Corona-Warn-App distribution service                                                    | NO ENDPOINT
postgres          | A [postgres] database installation                                                          | `localhost:8001` <br> `postgres:5432` (from containerized pgadmin) <br> Username: postgres <br> Password: postgres
pgadmin           | A [pgadmin](https://www.pgadmin.org/) installation for the postgres database                | `http://localhost:8002` <br> Username: user@domain.com <br> Password: password
cloudserver       | [Zenko CloudServer] is a S3-compliant object store  | `http://localhost:8003/` <br> Access key: accessKey1 <br> Secret key: verySecretKey1
verification-fake | A very simple fake implementation for the tan verification.                                 | `http://localhost:8004/version/v1/tan/verify` <br> The only valid tan is `edc07f08-a1aa-11ea-bb37-0242ac130002`.

##### Known Limitation

In rare cases the docker-compose runs into a timing issue if the distribution service starts before the bucket of the objectstore was created. This is not a big issue as you can simply run ```docker-compose run distribution``` to trigger additional distribution runs after the objectstore was initialized.

#### Running Single CWA Services Using Docker

If you would like to build and run a single CWA service, it's considered easiest to run them in a Docker environment. You can do this using the script provided in the respective CWA service directory. The Docker script first builds the CWA service and then creates an image for the runtime, which means that there are no additional dependencies for you to install.

To build and run the distribution service, run the following command:

```bash
./services/distribution/build_and_run.sh
```

To build and run the submission service, run the following command:

```bash
./services/submission/build_and_run.sh
```

The submission service is available on [localhost:8080](http://localhost:8080 ).

### Maven-Based Build

If you want to actively develop in one of the CWA services, the Maven-based runtime is most suitable.
To prepare your machine to run the CWA project locally, we recommend that you first ensure that you've installed the following:

* Minimum JDK Version 11: [OpenJDK](https://openjdk.java.net/) / [SapMachine](https://sap.github.io/SapMachine/)
* [Maven 3.6](https://maven.apache.org/)
* [Postgres]
* [Zenko CloudServer]

If you are already running a local Postgres, you need to create a database `cwa` and run the following setup scripts:

* Create the different CWA roles first by executing [create-roles.sql](setup/create-roles.sql).
* Create local database users for the specific roles by running [create-users.sql](./local-setup/create-users.sql).
* It is recommended to also run [enable-test-data-docker-compose.sql](./local-setup/enable-test-data-docker-compose.sql)
, which enables the test data generation profile. If you already had CWA running before and an existing `diagnosis-key`
table on your database, you need to run [enable-test-data.sql](./local-setup/enable-test-data.sql) instead.

You can also use `docker-compose` to start Postgres and Zenko. If you do that, you have to
set the following environment-variables when running the Spring project:

For the distribution module:

```bash
POSTGRESQL_SERVICE_PORT=8001
VAULT_FILESIGNING_SECRET=</path/to/your/private_key>
SPRING_PROFILES_ACTIVE=signature-dev,disable-ssl-client-postgres
```

For the submission module:

```bash
POSTGRESQL_SERVICE_PORT=8001
SPRING_PROFILES_ACTIVE=disable-ssl-server,disable-ssl-client-postgres,disable-ssl-client-verification,disable-ssl-client-verification-verify-hostname
```

#### Configure

After you made sure that the specified dependencies are running, configure them in the respective configuration files.

* Configure the Postgres connection in the [submission config](./services/submission/src/main/resources/application.yaml) and in the [distribution config](./services/distribution/src/main/resources/application.yaml)
* Configure the S3 compatible object storage in the [distribution config](./services/distribution/src/main/resources/application.yaml)
* Configure the private key for the distribution service, the path need to be prefixed with `file:`
  * `VAULT_FILESIGNING_SECRET` should be the path to the private key, example available in `<repo-root>/docker-compose-test-secrets/private.pem`

#### Build

After you've checked out the repository, to build the project, run ```mvn install``` in your base directory.

#### Run

Navigate to the service you want to start and run the spring-boot:run target. The configured Postgres and the configured S3 compliant object storage are used as default. When you start the submission service, the endpoint is available on your local port 8080.

If you want to start the submission service, for example, you start it as follows:

```bash
  cd services/submission/
  mvn spring-boot:run
```

#### Debugging

To enable the `DEBUG` log level, you can run the application using the Spring `dev` profile.

```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

To be able to set breakpoints (e.g. in IntelliJ), it may be necessary to use the ```-Dspring-boot.run.fork=false``` parameter.

## Service APIs

The API that is being exposed by the backend services is documented in an [OpenAPI](https://www.openapis.org/) specification. The specification files are available at the following locations:

Service                   | OpenAPI Specification
--------------------------|-------------
Submission Service        | [services/submission/api_v1.json](https://github.com/corona-warn-app/cwa-server/raw/master/services/submission/api_v1.json)
Distribution Service      | [services/distribution/api_v1.json](https://github.com/corona-warn-app/cwa-server/raw/master/services/distribution/api_v1.json)

## Spring Profiles

### Distribution

See [Distribution Service - Spring Profiles](/docs/DISTRIBUTION.md#spring-profiles).

### Submission

See [Submission Service - Spring Profiles](/docs/SUBMISSION.md#spring-profiles).

## Documentation

The full documentation for the Corona-Warn-App can be found in the [cwa-documentation](https://github.com/corona-warn-app/cwa-documentation) repository. The documentation repository contains technical documents, architecture information, and whitepapers related to this implementation.

The documentation for cwa-server can be found under the [/docs](./docs) folder.

The JavaDoc documentation for cwa-server is hosted by Github Pages at [https://corona-warn-app.github.io/cwa-server](https://corona-warn-app.github.io/cwa-server).

## Support and Feedback

The following channels are available for discussions, feedback, and support requests:

| Type                     | Channel                                                |
| ------------------------ | ------------------------------------------------------ |
| **General Discussion**   | <a href="https://github.com/corona-warn-app/cwa-documentation/issues/new/choose" title="General Discussion"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-documentation/question.svg?style=flat-square"></a> </a>   |
| **Concept Feedback**    | <a href="https://github.com/corona-warn-app/cwa-documentation/issues/new/choose" title="Open Concept Feedback"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-documentation/architecture.svg?style=flat-square"></a>  |
| **Backend Issue**    | <a href="https://github.com/corona-warn-app/cwa-server/issues/new/choose" title="Open Backend Issue"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-server?style=flat-square"></a>  |
| **Other Requests**    | <a href="mailto:corona-warn-app.opensource@sap.com" title="Email CWA Team"><img src="https://img.shields.io/badge/email-CWA%20team-green?logo=mail.ru&style=flat-square&logoColor=white"></a>   |

## How to Contribute

Contribution and feedback are encouraged and always welcome. For more information about how to contribute, the project structure, as well as additional contribution information, see our [Contribution Guidelines](./CONTRIBUTING.md). By participating in this project, you agree to abide by its [Code of Conduct](./CODE_OF_CONDUCT.md) at all times.

## Contributors

The German government has asked SAP and Deutsche Telekom to develop the Corona-Warn-App for Germany as open source software. Deutsche Telekom is providing the network and mobile technology and will operate and run the backend for the app in a safe, scalable and stable manner. SAP is responsible for the app development, its framework and the underlying platform. Therefore, development teams of SAP and Deutsche Telekom are contributing to this project. At the same time our commitment to open source means that we are enabling -in fact encouraging- all interested parties to contribute and become part of its developer community.

## Repositories

The following public repositories are currently available for the Corona-Warn-App:

| Repository          | Description                                                           |
| ------------------- | --------------------------------------------------------------------- |
| [cwa-documentation] | Project overview, general documentation, and white papers            |
| [cwa-server]        | Backend implementation for the Apple/Google exposure notification API|
| [cwa-verification-server] | Backend implementation of the verification process|

[cwa-documentation]: https://github.com/corona-warn-app/cwa-documentation
[cwa-server]: https://github.com/corona-warn-app/cwa-server
[cwa-verification-server]: https://github.com/corona-warn-app/cwa-verification-server
[Postgres]: https://www.postgresql.org/
[HSQLDB]: http://hsqldb.org/
[Zenko CloudServer]: https://github.com/scality/cloudserver

## Licensing

Copyright (c) 2020 SAP SE or an SAP affiliate company.

Licensed under the **Apache License, Version 2.0** (the "License"); you may not use this file except in compliance with the License.

You may obtain a copy of the License at <https://www.apache.org/licenses/LICENSE-2.0>.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the [LICENSE](./LICENSE) for the specific language governing permissions and limitations under the License.
