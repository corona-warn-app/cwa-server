FROM openjdk:11 as build

ARG WORK_DIR=/build

# copy the pom and config files first (which do not change very often)
COPY ./.mvn ${WORK_DIR}/.mvn
COPY ./mvnw ${WORK_DIR}/mvnw
COPY ./common/pom.xml ${WORK_DIR}/common/pom.xml
COPY ./common/protocols/pom.xml ${WORK_DIR}/common/protocols/pom.xml
COPY ./common/persistence/pom.xml ${WORK_DIR}/common/persistence/pom.xml
COPY ./common/federation/pom.xml ${WORK_DIR}/common/federation/pom.xml
COPY ./services/pom.xml ${WORK_DIR}/services/pom.xml
COPY ./services/distribution/pom.xml ${WORK_DIR}/services/distribution/pom.xml
COPY ./services/submission/pom.xml ${WORK_DIR}/services/submission/pom.xml
COPY ./services/callback/pom.xml ${WORK_DIR}/services/callback/pom.xml
COPY ./services/download/pom.xml ${WORK_DIR}/services/download/pom.xml
COPY ./services/upload/pom.xml ${WORK_DIR}/services/upload/pom.xml
COPY ./pom.xml ${WORK_DIR}/pom.xml

WORKDIR ${WORK_DIR}

# build all dependencies for offline use
RUN ${WORK_DIR}/mvnw dependency:go-offline --file ${WORK_DIR}/pom.xml -B

# copy rest of files
COPY . ${WORK_DIR}/

# hadolint ignore=SC2086
RUN ${WORK_DIR}/mvnw --batch-mode -P docker-image --file ${WORK_DIR}/pom.xml ${MAVEN_ARGS} clean verify

# copy .jars in service specific folders
RUN mkdir -p /usr/sap/download-service
RUN cp ${WORK_DIR}/services/download/target/*.jar /usr/sap/download-service/service.jar
RUN mkdir -p /usr/sap/submission-service
RUN cp ${WORK_DIR}/services/submission/target/*.jar /usr/sap/submission-service/service.jar
RUN mkdir -p /usr/sap/callback-service
RUN cp ${WORK_DIR}/services/callback/target/*.jar /usr/sap/callback-service/service.jar
RUN mkdir -p /usr/sap/distribution-service
RUN cp ${WORK_DIR}/services/distribution/target/*.jar /usr/sap/distribution-service/service.jar
RUN mkdir -p /usr/sap/upload-service
RUN cp ${WORK_DIR}/services/upload/target/*.jar /usr/sap/upload-service/service.jar
RUN cp ${WORK_DIR}/scripts/DpkgHelper.java /DpkgHelper.java


