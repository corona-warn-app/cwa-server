FROM maven:3.6.3-jdk-11 as build

ARG WORK_DIR=/build

COPY . ${WORK_DIR}/
WORKDIR ${WORK_DIR}

RUN mkdir -p /root/.m2 /usr/sap/distribution-service
RUN cd ${WORK_DIR}
RUN mvn -B -DskipTests=true ${MAVEN_ARGS} clean install
RUN cp ${WORK_DIR}/services/distribution/target/distribution-*.jar /usr/sap/distribution-service/distribution.jar

FROM gcr.io/distroless/java:11
COPY --from=build /usr/sap/distribution-service/distribution.jar .
CMD ["distribution.jar"]
