FROM openjdk:11 as build

ARG WORK_DIR=/build
ARG SERVICE=download

# copy the pom and config files first (which do not change very often)
COPY ./.mvn ${WORK_DIR}/.mvn
COPY ./mvnw ${WORK_DIR}/mvnw
COPY ./common/pom.xml ${WORK_DIR}/common/pom.xml
COPY ./common/protocols/pom.xml ${WORK_DIR}/common/protocols/pom.xml
COPY ./common/persistence/pom.xml ${WORK_DIR}/common/persistence/pom.xml
COPY ./common/federation/pom.xml ${WORK_DIR}/common/federation/pom.xml
COPY ./common/jupiterHelpers/pom.xml ${WORK_DIR}/common/jupiterHelpers/pom.xml
COPY ./common/shared/pom.xml ${WORK_DIR}/common/shared/pom.xml
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

RUN mkdir -p /usr/sap/${SERVICE}-service
# hadolint ignore=SC2086
RUN ${WORK_DIR}/mvnw --batch-mode -P docker-image --file ${WORK_DIR}/pom.xml ${MAVEN_ARGS} clean verify
RUN cp ${WORK_DIR}/services/${SERVICE}/target/*.jar /usr/sap/${SERVICE}-service/service.jar
RUN cp ${WORK_DIR}/scripts/DpkgHelper.java /DpkgHelper.java

FROM gcr.io/distroless/java-debian10:11

ARG SERVICE=download

WORKDIR /
COPY --from=build /DpkgHelper.java .
COPY --from=build /usr/sap/${SERVICE}-service/service.jar .
RUN ["java", "DpkgHelper.java"]
USER 65534:65534
ENTRYPOINT ["/usr/bin/java", "-Djdk.tls.namedGroups=\"secp256r1,secp384r1,brainpoolP256r1,brainpoolP384r1,brainpoolP512r1,ffdhe2048,ffdhe3072,ffdhe4096\"", "-jar"]
CMD ["service.jar"]
