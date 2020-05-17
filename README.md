<h1 align="center">
    Corona-Warn-App Server
</h1>

<p align="center">
    <a href="https://github.com/Exposure-Notification-App/ena-documentation/commits/" title="Last Commit"><img src="https://img.shields.io/github/last-commit/corona-warn-app/cwa-server?style=flat"></a>
    <a href="https://github.com/Exposure-Notification-App/ena-documentation/issues" title="Open Issues"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-server?style=flat"></a>
    <a href="https://circleci.com/gh/corona-warn-app/cwa-server" title="Build Status"><img src="https://circleci.com/gh/corona-warn-app/cwa-server.svg?style=shield&circle-token=4ab059989d10709df19eb4b98ab7c121a25e981a"></a>
    <a href="https://github.com/corona-warn-app/cwa-server/blob/master/LICENSE" title="License"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg?style=flat"></a>
</p>

<p align="center">
  <a href="#development">Development</a> •
  <a href="#service-apis">Service APIs</a> •
  <a href="#documentation">Documentation</a> •
  <a href="#support--feedback">Support</a> •
  <a href="#how-to-contribute">How to Contribute</a> •
  <a href="#contributors">Contributors</a> •
  <a href="#repositories">Repositories</a> •
  <a href="#licensing">Licensing</a>
</p>

The goal of this project is to develop the official Corona-Warn-App for Germany based on the exposure notification API from [Apple](https://www.apple.com/covid19/contacttracing/) and [Google](https://www.google.com/covid19/exposurenotifications/). The apps (for both iOS and Android) use Bluetooth technology to exchange anonymous encrypted data with other mobile phones (on which the app is also installed) in the vicinity of an app user's phone. The data is stored locally on each user's device, preventing authorities or other parties from accessing or controlling the data. This repository contains the **implementation of the server for encryption keys** for the Corona-Warn-App. This implementation is still a **work in progress**, and the code it contains is currently alpha-quality code.

In this documentation, Corona-Warn-App services are also referred to as cwa services.

## Development

After you've checked out this repository, you can run the application in one of the following ways:

* As a [Docker](https://www.docker.com/)-based deployment on your local machine. You can run either:
  * Single components using the respective Dockerfile or
  * The full backend using the Docker Compose (which is considered the most convenient way)
* As a [Maven](https://maven.apache.org)-based build on your local machine.
  If you want to develop something in a single component, this approach is preferable.

### Docker-Based Deployment

If you want to use Docker-based deployment, you need to install Docker on your local machine. For more information about downloading and installing Docker, see the [official Docker documentation](https://docs.docker.com/get-docker/).

#### Running the Full cwa Backend Using Docker Compose

For your convenience, a full setup including the generation of test data has been prepared using [Docker Compose](https://docs.docker.com/compose/reference/overview/). To build the backend services, run ```docker-compose build``` in the repository's root directory. A default configuration file can be found under ```.env```in the root folder of the repository. The default values for the local Postgres and MinIO build should be changed in this file before docker-compose is run.

Once the services are built, you can start the whole backend using ```docker-compose up```.
The distribution service runs once and then finishes. If you want to trigger additional distribution runs, run ```docker-compose start distribution```.

The docker-compose contains the following services:

Service       | Description | Endpoint and Default Credentials
--------------|-------------|-----------
submission    | The Corona-Warn-App submission service                                            | http://localhost:8080 
distribution  | The Corona-Warn-App distribution service                                          | NO ENDPOINT
postgres      | A [postgres] database installation                                                | postgres:5432 <br> Username: postgres <br> Password: postgres
pgadmin       | A [pgadmin](https://www.pgadmin.org/) installation for the postgres database      | http://localhost:8081 <br> Username: user@domain.com <br> Password: password
minio         | [MinIO] is an S3-compliant object store                                           | http://localhost:8082/ <br> Access key: cws_key_id <br> Secret key: cws_secret_key_id

#### Running Single cwa Services Using Docker

If you would like to build and run a single cwa service, it's considered easiest to run them in a Docker environment. You can do this using the script provided in the respective cwa service directory. The Docker script first builds the cwa service and then creates an image for the runtime, which means that there are no additional dependencies for you to install.

To build and run the distribution service, run the following command:

```bash
./services/distribution/build_and_run.sh
```

To build and run the submission service, run the following command:

```bash
./services/submission/build_and_run.sh
```

The submission service is available on localhost:8080.

### Maven-Based Build

If you want to actively develop in one of the cwa services, the Maven-based runtime is most suitable.
To prepare your machine to run the cwa project locally, we recommend that you first ensure that you've installed the following:

* [Minimum JDK Version 11](https://openjdk.java.net/)
* [Maven 3.6](https://maven.apache.org/)
* [Postgres] (if you want to connect to a persistent storage; if a postgres connection is not specified, an in-memory [HSQLDB] is provided)
* [MinIO] (if you want to run the distribution service and write the files to an object store instead of using your local file system)

#### Build

After you've checked out the repository, to build the project, run ```mvn install``` in your base directory.

### Run

Navigate to the service you want to start and run the spring-boot:run target. The HSQLDB is used by default.
When you start the submission service, the endpoint is available on your local port 8080.

If you want to start the submission service, for example, you start it as follows:

```bash
  cd services/submission/
  mvn spring-boot:run
```

If you want to use a Postgres database instead of the default in-memory HSQLDB, use the Postgres profile when starting the application:

```bash
  cd services/submission/
  mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

To enable the S3-compatible object storage integration in the cwa distribution service, use the S3 profile when starting the application:

```bash
  cd services/distribution/
  mvn spring-boot:run -Dspring-boot.run.profiles=s3
```

You can also combine multiple profiles if necessary:

```bash
  cd services/distribution/
  mvn spring-boot:run -Dspring-boot.run.profiles=dev,postgres,s3
```

### Debugging

To enable the `DEBUG` log level, you can run the application using the Spring `dev` profile.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

To be able to set breakpoints (e.g. in IntelliJ), it may be necessary to use the ```-Dspring-boot.run.fork=false``` parameter.

## Service APIs

The API that is being exposed by the backend services is documented in an [OpenAPI](https://www.openapis.org/) specification. The specification files are available at the following locations: 

Service      | OpenAPI Specification
-------------|-------------
Submission Service        | https://github.com/corona-warn-app/cwa-server/raw/master/services/submission/api_v1.json
Distribution Service      | https://github.com/corona-warn-app/cwa-server/raw/master/services/distribution/api_v1.json

## Documentation

The full documentation for the Corona-Warn-App can be found in the [cwa-documentation] repository. This repository contains technical documents, UI/UX specifications, architecture information, and whitepapers for this implementation.

## Support and Feedback

The following channels are available for discussions, feedback, and support requests:

| Type                     | Channel                                                |
| ------------------------ | ------------------------------------------------------ |
| **General Discussion**   | <a href="https://github.com/corona-warn-app/cwa-documentation/issues/new/choose" title="General Discussion"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-documentation/question.svg?style=flat-square"></a> </a>   |
| **Concept Feedback**    | <a href="https://github.com/corona-warn-app/cwa-documentation/issues/new/choose" title="Open Concept Feedback"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-documentation/concept-extension.svg?style=flat-square"></a>  |
| **Backend Issue**    | <a href="https://github.com/corona-warn-app/cwa-server/issues/new/choose" title="Open Backend Issue"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-server/backend.svg?style=flat-square"></a>  |
| **Other Requests**    | <a href="mailto:corona-warn-app.opensource@sap.com" title="Email CWD Team"><img src="https://img.shields.io/badge/email-CWD%20team-green?logo=mail.ru&style=flat-square&logoColor=white"></a>   |

## How to Contribute

Contribution and feedback are encouraged and always welcome. For more information about how to contribute, the project structure, as well as additional contribution information, see our [Contribution Guidelines](./CONTRIBUTING.md). By participating in this project, you agree to abide by its [Code of Conduct](./CODE_OF_CONDUCT.md) at all times.

## Contributors

The German government requested SAP SE and Deutsche Telekom AG to develop the Corona-Warn-App. Deutsche Telekom is providing the infrastructure technology and is to operate and run the backend for the app in a safe, scalable, and stable manner. SAP is responsible for app development and the exposure notification backend. Development teams from both SAP and Deutsche Telekom are therefore contributing to this project. At the same time, commitment to open source means that all interested parties are being enabled **and encouraged** to contribute and become part of this developer community.

## Repositories

The following public repositories are currently available for the Corona-Warn-App:

| Repository          | Description                                                           |
| ------------------- | --------------------------------------------------------------------- |
| [cwa-documentation] | Project overview, general documentation, and white papers            |
| [cwa-server]        | Backend implementation for the Apple/Google exposure notification API|

[cwa-documentation]: https://github.com/corona-warn-app/cwa-documentation
[cwa-server]: https://github.com/corona-warn-app/cwa-server
[Postgres]: https://www.postgresql.org/
[MinIO]: https://min.io/
[HSQLDB]: http://hsqldb.org/
---

## Licensing

This project is licensed under the **Apache 2.0** license. For more information, see the [LICENSE](./LICENSE) file.
