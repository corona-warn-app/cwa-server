FROM preparation:1 as build

ARG WORK_DIR=/build
ARG SERVICE=upload

WORKDIR ${WORK_DIR}

# hadolint ignore=SC2086
RUN cp ${WORK_DIR}/services/${SERVICE}/target/*.jar /usr/sap/${SERVICE}-service/service.jar
RUN cp ${WORK_DIR}/scripts/DpkgHelper.java /DpkgHelper.java

FROM gcr.io/distroless/java-debian10:11

#copy and run jars
ARG SERVICE=upload
COPY --from=build /DpkgHelper.java .
COPY --from=build /usr/sap/${SERVICE}-service/service.jar .
RUN ["java", "DpkgHelper.java"]
USER 65534:65534
ENTRYPOINT ["/usr/bin/java", "-Djdk.tls.namedGroups=\"secp256r1,secp384r1,brainpoolP256r1,brainpoolP384r1,brainpoolP512r1,ffdhe2048,ffdhe3072,ffdhe4096\"", "-jar"]
CMD ["service.jar"]
