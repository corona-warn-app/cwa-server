FROM maven:3.6.3-jdk-11 as build

ARG WORK_DIR=/build

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
CMD ["distribution.jar"]
