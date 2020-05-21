FROM maven:3.6.3-jdk-11 as build

ARG WORK_DIR=/build

COPY . ${WORK_DIR}/
WORKDIR ${WORK_DIR}

RUN mkdir -p /root/.m2 /usr/sap/submission-service
RUN cd ${WORK_DIR}
RUN mvn -B -DskipTests=true ${MAVEN_ARGS} clean install
RUN cp ${WORK_DIR}/services/submission/target/submission-*.jar /usr/sap/submission-service/submission.jar

FROM gcr.io/distroless/java:11
COPY --from=build /usr/sap/submission-service/submission.jar .
CMD ["submission.jar"]
