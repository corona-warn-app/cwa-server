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
  <a href="#service-apis">Service APIs</a> •
  <a href="#development">Development</a> •
  <a href="#architecture--documentation">Documentation</a> •
  <a href="#contributing">Contributing</a> •
  <a href="#support--feedback">Support</a> •
  <a href="https://github.com/corona-warn-app/cwa-admin/releases">Changelog</a>
</p>

This project has the goal to develop the official Corona-Warn-App for Germany based on the Exposure Notification API by [Apple](https://www.apple.com/covid19/contacttracing/) and [Google](https://www.google.com/covid19/exposurenotifications/).  The apps (for both iOS and Android) will collect anonymous data from nearby mobile phones using Bluetooth technology. The data will be stored locally on each device, preventing authorities’ access and control over tracing data. This repository contains the **implementation of the key server** for the Corona-Warn-App. This implementation is **work in progress** and contains alpha-quality code only.

## Service APIs

Service      | OpenAPI Specification
-------------|-------------
Submission Service        | https://github.com/corona-warn-app/cwa-server/raw/master/services/submission/api_v1.json
Distribution Service      | https://github.com/corona-warn-app/cwa-server/raw/master/services/distribution/api_v1.json


## Development

### Setup

To setup this project locally on your machine, we recommend to install the following prerequisites:
  - Java OpenJDK 11
  - Maven 3.6
  - Docker if you would like to build Docker images
  - Postgres if you would like to connect a persistent storage. If no postgres connection is specified an in-memory HSQLDB will be provided.

### Build

After you checked out the repository run ```mvn install``` in your base directory to build the project.

### Run

Navigate to the service you would like to start and run the spring-boot:run target. By default the HSQLDB will be used and after the Spring Boot application started the endpoint will be available on your local port 8080. As an example if you would like to start the submission service run:
```
  cd services/submission/
  mvn spring-boot:run
```

If you want to use a postgres DB instead please use the postgres profile when starting the application:

```
  cd services/submission/
  mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

In order to enable S3 integration, you will need the following vars in your env:

Var | Description
----|----------------
AWS_ACCESS_KEY_ID | The access key
AWS_SECRET_ACCESS_KEY | The secret access key
cwa.objectstore.endpoint | The S3 endpoint
cwa.objectstore.bucket | The S3 bucket name

### Build and run Docker Images

First download and install [Docker](https://www.docker.com/products/docker-desktop).

If you want to build and run a docker image of a service you can use the prepared script in the respective service directory.
To build and run the distribution service:
```
./services/distribution/build_and_run.sh
```

To build and run the submission service:
```
./services/submission/build_and_run.sh
```
The submission service will then be available locally on port 8080.

## Known Issues

There are no known issues.

## Architecture & Documentation

The full documentation for the Corona-Warn-App is in the [cwa-documentation](https://github.com/corona-warn-app/cwa-documentation) repository. Please refer to this repository for technical documents, UI/UX specifications, architectures, and whitepapers of this implementation.

## Support & Feedback

| Type                     | Channel                                                |
| ------------------------ | ------------------------------------------------------ |
| **General Discussion**   | <a href="https://github.com/corona-warn-app/cwa-documentation/issues/new/choose" title="General Discussion"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-documentation/question.svg?style=flat-square"></a> </a>   |
| **Concept Feedback**    | <a href="https://github.com/corona-warn-app/cwa-documentation/issues/new/choose" title="Open Concept Feedback"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-documentation/concept-extension.svg?style=flat-square"></a>  |
| **iOS App Issue**    | <a href="https://github.com/corona-warn-app/cwa-app-ios/issues/new/choose" title="Open iOS Suggestion"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-app-ios/ios-app.svg?style=flat-square"></a>  |
| **Android App Issue**    | <a href="https://github.com/corona-warn-app/cwa-app-android/issues/new/choose" title="Open Android Issue"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-app-android/android-app.svg?style=flat-square"></a>  |
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
| [cwa-app-ios]       | Native iOS app using the Apple/Google exposure notification API.      |
| [cwa-app-android]   | Native Android app using the Apple/Google exposure notification API.  |
| [cwa-server]        | Backend implementation for the Apple/Google exposure notification API.|

[cwa-documentation]: https://github.com/corona-warn-app/cwa-documentation
[cwa-app-ios]: https://github.com/corona-warn-app/cwa-app-ios
[cwa-app-android]: https://github.com/corona-warn-app/cwa-app-android
[cwa-server]: https://github.com/corona-warn-app/cwa-server

---

This project is licensed under the **Apache-2.0** license. For more information, see the [LICENSE](./LICENSE) file.
