<h1 align="center">
    Corona Warn App - Server
</h1>

<p align="center">
    <a href="https://github.com/Exposure-Notification-App/ena-documentation/commits/" title="Last Commit"><img src="https://img.shields.io/github/last-commit/corona-warn-app/cwa-server?style=flat"></a>
    <a href="https://github.com/Exposure-Notification-App/ena-documentation/issues" title="Open Issues"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-server?style=flat"></a>
    <a href="https://circleci.com/gh/corona-warn-app/cwa-server" title="Build Status"><img src="https://circleci.com/gh/corona-warn-app/cwa-server.svg?style=flat&circle-token=a7294b977bb9ea2c2d53ff62c9aa442670e19b59"></a>
    <a href="https://github.com/corona-warn-app/cwa-server/blob/master/LICENSE" title="License"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg?style=flat"></a>
</p>

<p align="center">
  <a href="#development">Development</a> •
  <a href="#service-apis">Service APIs</a> •
  <a href="#architecture--documentation">Documentation</a> •
  <a href="#support--feedback">Support</a> •
  <a href="#contributing">Contributing</a>
</p>

This project has the goal to develop the official Corona-Warn-App for Germany based on the Exposure Notification API by [Apple](https://www.apple.com/covid19/contacttracing/) and [Google](https://www.google.com/covid19/exposurenotifications/).  The apps (for both iOS and Android) will collect anonymous data from nearby mobile phones using Bluetooth technology. The data will be stored locally on each device, preventing authorities’ access and control over tracing data. This repository contains the **implementation of the key server** for the Corona-Warn-App. This implementation is **work in progress** and contains alpha-quality code only.

The Corona Warn App services are commonly referred to as cwa services in this documentation.

## Development

After you checkout this repository, we support two ways of running the application:

1. A [docker](https://www.docker.com/) based deployment on your local machine. You can either run the single components using the respective dockerfile or if you want to run the full backend using the docker-compose might be the most convenient way.
2. A maven based runtime on your local machine. If you want to develop something in a single component this is the proposed approach.

### Docker based deployment

If you would like to use the docker based deployment you need to install docker on your local machine. To do so please consult the [official docker documentation](https://docs.docker.com/get-docker/) to download and install.

#### Run the full cwa backend using docker compose

We prepared a full setup for your convenience using [docker-compose](https://docs.docker.com/compose/reference/overview/). To build the backend services run ```docker-compose build``` in the repositories root directory. After the services were built you can start the whole backend using ```docker-compose up```.
The distribution service will only run once and then finish. If you want to trigger additional distribution runs please run ```docker-compose start distribution```.

The docker compose contains the following services:

Service       | Description | Endpoint & Default Credentials
--------------|-------------|-----------
submission    | The Corona Warn App submission service                                      | http://localhost:8080 
distribution  | The Corona Warn App distribution service                                    | NO ENDPOINT
postgres      | A [postgres] database installation             | postgres:5432
pgadmin       | A [pgadmin](https://www.pgadmin.org/) installation for the postgres db      | http://localhost:8081 <br> Username: user@domain.com <br> Password: password
minio         | [MinIO] is a S3 compliant object store                                      | http://localhost:8082/ <br> Access Key: cws_key_id <br> Secret Key: cws_secret_key_id

#### Run single cwa services using docker

If you would like to build and run a single cwa service it might be easiest to run them in a docker environment. For this you can use the prepared script in the respective cwa service directory. The docker script will first build the cwa service and then create an image for the runtime. Therefore, there are no additional dependencies for you to install.

To build and run the distribution service run:

```bash
./services/distribution/build_and_run.sh
```

To build and run the submission service run:

```bash
./services/submission/build_and_run.sh
```

The submission service will be available on localhost:8080.

### Maven based runtime

The maven based runtime is the right choice if you want to actively develop in one of the cwa services.
To prepare your machine to run the cwa project locally, we recommend to install the following prerequisites:

- [Java OpenJDK 11](https://openjdk.java.net/)
- [Maven 3.6](https://maven.apache.org/)
- [Postgres] if you would like to connect a persistent storage. If no postgres connection is specified an in-memory [HSQLDB](http://hsqldb.org/) will be provided.
- [MinIO] if you would like to run the distribution service and write the files to an object store instead of using your local file system.

#### Build

After you checked out the repository run ```mvn install``` in your base directory to build the project.

### Run

Navigate to the service you would like to start and run the spring-boot:run target. By default the HSQLDB will be used and after the Spring Boot application started the endpoint will be available on your local port 8080. As an example if you would like to start the submission service run:

```bash
  cd services/submission/
  mvn spring-boot:run
```

If you want to use a postgres DB instead please use the postgres profile when starting the application:

```bash
  cd services/submission/
  mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

In order to enable the S3 integration in the cwa distribution service, please use the s3 profile when starting the application.

```bash
  cd services/distribution/
  mvn spring-boot:run -Dspring-boot.run.profiles=s3
```

Of course you can combine multiple profiles.

```bash
  cd services/distribution/
  mvn spring-boot:run -Dspring-boot.run.profiles=dev, postgres,s3
```

### Debugging

You may run the application with the spring profile `dev` to enable the `DEBUG` log-level.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

To be able to set breakpoints (e.g. in IntelliJ), it may be necessary to use the parameter ```-Dspring-boot.run.fork=false```.

## Service APIs

The API which is being exposed by our backend services is documented in an [OpenAPI](https://www.openapis.org/) specification. The following table shows the location where you can find the specification files:

Service      | OpenAPI Specification
-------------|-------------
Submission Service        | https://github.com/corona-warn-app/cwa-server/raw/master/services/submission/api_v1.json
Distribution Service      | https://github.com/corona-warn-app/cwa-server/raw/master/services/distribution/api_v1.json

## Architecture & Documentation

The full documentation for the Corona-Warn-App can be found in the [cwa-documentation] repository. Please refer to this repository for technical documents, UI/UX specifications, architectures, and whitepapers of this implementation.

## Support & Feedback

| Type                     | Channel                                                |
| ------------------------ | ------------------------------------------------------ |
| **General Discussion**   | <a href="https://github.com/corona-warn-app/cwa-documentation/issues/new/choose" title="General Discussion"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-documentation/question.svg?style=flat-square"></a> </a>   |
| **Concept Feedback**    | <a href="https://github.com/corona-warn-app/cwa-documentation/issues/new/choose" title="Open Concept Feedback"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-documentation/concept-extension.svg?style=flat-square"></a>  |
| **Backend Issue**    | <a href="https://github.com/corona-warn-app/cwa-server/issues/new/choose" title="Open Backend Issue"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-server/backend.svg?style=flat-square"></a>  |
| **Other Requests**    | <a href="mailto:corona-warn-app.opensource@sap.com" title="Email CWD Team"><img src="https://img.shields.io/badge/email-CWD%20team-green?logo=mail.ru&style=flat-square&logoColor=white"></a>   |

## Contributing

Contributions and feedback are encouraged and always welcome. Please see our [Contribution Guidelines](./CONTRIBUTING.md) for details on how to contribute, the project structure and additional details you need to know to work with us. By participating in this project, you agree to abide by its [Code of Conduct](./CODE_OF_CONDUCT.md).

## Contributors

The German government has asked SAP and Deutsche Telekom to develop the Corona-Warn-App. Deutsche Telekom is providing the infrastructure technology and will operate and run the backend for the app in a safe, scalable, and stable manner. SAP is responsible for the development of the app development and the exposure notification backend. Therefore, development teams of SAP and T-Systems are contributing to this project. At the same time, our commitment to open source means that we are enabling -and encouraging- all interested parties to contribute and become part of its developer community. 

## Repositories

| Repository          | Description                                                           |
| ------------------- | --------------------------------------------------------------------- |
| [cwa-documentation] | Project overview, general documentation, and white papers.            |
| [cwa-server]        | Backend implementation for the Apple/Google exposure notification API.|

[cwa-documentation]: https://github.com/corona-warn-app/cwa-documentation
[cwa-server]: https://github.com/corona-warn-app/cwa-server
[Postgres]: https://www.postgresql.org/
[MinIO]: https://min.io/
---

This project is licensed under the **Apache-2.0** license. For more information, see the [LICENSE](./LICENSE) file.
