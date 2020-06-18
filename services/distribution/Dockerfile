FROM maven:3.6.3-jdk-11 as build

ARG WORK_DIR=/build

# copy the pom and config files first (which do not change very often)
COPY ./.mvn ${WORK_DIR}/.mvn
COPY ./pom.xml ${WORK_DIR}/pom.xml
COPY ./common/pom.xml ${WORK_DIR}/common/pom.xml
COPY ./common/protocols/pom.xml ${WORK_DIR}/common/protocols/pom.xml
COPY ./common/persistence/pom.xml ${WORK_DIR}/common/persistence/pom.xml
COPY ./services/pom.xml ${WORK_DIR}/services/pom.xml
COPY ./services/distribution/pom.xml ${WORK_DIR}/services/distribution/pom.xml
COPY ./services/submission/pom.xml ${WORK_DIR}/services/submission/pom.xml

# build all dependencies for offline use
RUN mvn dependency:go-offline --file ${WORK_DIR}/pom.xml -B

# copy rest of files
COPY . ${WORK_DIR}/

RUN mkdir -p /root/.m2 /usr/sap/distribution-service
# hadolint ignore=SC2086
RUN mvn --batch-mode -DskipTests=true --file ${WORK_DIR}/pom.xml ${MAVEN_ARGS} clean install
RUN cp ${WORK_DIR}/services/distribution/target/distribution-*.jar /usr/sap/distribution-service/distribution.jar
RUN cp ${WORK_DIR}/scripts/DpkgHelper.java /DpkgHelper.java

FROM gcr.io/distroless/java:11
COPY --from=build /DpkgHelper.java .
COPY --from=build /usr/sap/distribution-service/distribution.jar .
RUN ["java", "DpkgHelper.java"]
USER 65534:65534
CMD ["distribution.jar"]
