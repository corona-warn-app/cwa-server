FROM openjdk:11 as build
ARG WORK_DIR=/build

# copy the pom and config files first (which do not change very often)
COPY ./.mvn ${WORK_DIR}/.mvn
COPY ./mvnw ${WORK_DIR}/mvnw
COPY ./pom.xml ${WORK_DIR}/pom.xml
COPY ./common/pom.xml ${WORK_DIR}/common/pom.xml
COPY ./common/protocols/pom.xml ${WORK_DIR}/common/protocols/pom.xml
COPY ./common/persistence/pom.xml ${WORK_DIR}/common/persistence/pom.xml
COPY ./services/pom.xml ${WORK_DIR}/services/pom.xml
COPY ./services/distribution/pom.xml ${WORK_DIR}/services/distribution/pom.xml
COPY ./services/submission/pom.xml ${WORK_DIR}/services/submission/pom.xml

WORKDIR ${WORK_DIR}

# build all dependencies for offline use
RUN ${WORK_DIR}/mvnw dependency:go-offline --file ${WORK_DIR}/pom.xml -B

# copy rest of files
COPY . ${WORK_DIR}/

RUN mkdir -p /usr/sap/submission-service
# hadolint ignore=SC2086
RUN ${WORK_DIR}/mvnw --batch-mode -DskipTests=true --file ${WORK_DIR}/pom.xml ${MAVEN_ARGS} clean install
RUN cp ${WORK_DIR}/services/submission/target/submission-*.jar /usr/sap/submission-service/submission.jar
RUN cp ${WORK_DIR}/scripts/DpkgHelper.java /DpkgHelper.java

FROM gcr.io/distroless/java:11
COPY --from=build /DpkgHelper.java .
COPY --from=build /usr/sap/submission-service/submission.jar .
RUN ["java", "DpkgHelper.java"]
USER 65534:65534
ENTRYPOINT ["/usr/bin/java", "-Djdk.tls.namedGroups=\"secp256r1,secp384r1,brainpoolP256r1,brainpoolP384r1,brainpoolP512r1,ffdhe2048,ffdhe3072,ffdhe4096\"", "-jar"]
CMD ["submission.jar"]
