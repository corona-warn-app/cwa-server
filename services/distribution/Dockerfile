FROM openjdk:11 as build

ARG WORK_DIR=/build

COPY . ${WORK_DIR}/
WORKDIR ${WORK_DIR}

RUN mkdir -p /usr/sap/distribution-service
RUN cd ${WORK_DIR}
RUN ./mvnw -B -DskipTests=true ${MAVEN_ARGS} clean install
RUN cp ${WORK_DIR}/services/distribution/target/distribution-*.jar /usr/sap/distribution-service

FROM gcr.io/distroless/java:11
COPY --from=build /usr/sap/distribution-service/distribution-*.jar .
CMD ["distribution-0.3.3-SNAPSHOT.jar"]
