# FAQ & Troubleshooting

This document gathers common problems and pitfalls, as well as possible solutions.

<u>The debugger does not attach to services / The debugger does not stop at breakpoints.</u>

* Try adding `-Dspring-boot.run.fork=false` to the services run config. E.g.: `spring-boot:run -Dspring-boot.run.fork=false`.

<u>Some Classes, Fields or Methods are (suddenly) undefined.</u>

* Try rebuilding the maven persistence package either by using ``$ mvn clean install`` in the root folder or in `<root>/common/persistence`.

<u>Migration-related Flyway errors occur, e.g.: org.flywaydb.core.api.FlywayException: Validate failed: </u>

* Stop the postgres docker container and delete it, as well as the corresponding docker volume (`$ docker volumes prune`).
Then rebuild and restart the container.

<u>After updating Docker, the container do not start properly or cant be interacted with</u>

* Delete all containers and volumes (`$ docker-compose down && docker system prune`). Rebuild all containers. If the problem persists, downgrade to the previous version of Docker.

<u>Eclipse-related errors:</u>

```text
[ERROR] /build/common/protocols/src/main/proto/app/coronawarn/server/common/protocols/internal/risk_score_classification.proto `
[0:0]: /build/common/protocols/target/protoc-plugins/protoc-3.13.0-osx-x86_64.exe:
5: /build/common/protocols/target/protoc-plugins/protoc-3.13.0-osx-x86_64.exe:
Syntax error: Unterminated quoted string
```

* Remove `<os.detected.classifier>osx-x86_64</os.detected.classifier>` in pom.xml.

 ```text
1 problem was encountered while building the effective model for app.coronawarn.server:persistence:${revision}
[ERROR] 'dependencies.dependency.version' for app.coronawarn.server:protocols:jar must be a valid version but is '${revision}'. @
 (org.codehaus.mojo:flatten-maven-plugin:1.2.5:flatten:flatten:process-resources)
```

* Add "<revision>1.6.0-SNAPSHOT</revision>" to pom.xml

```text
<Many Different Java Classes> cannot be resolved, DiagnosisKeyBatch cannot be resolved
```

* Remove "<revision>1.6.0-SNAPSHOT</revision>" to pom.xml

```text
Plugin execution not covered by lifecycle configuration: org.codehaus.mojo:flatten-maven-plugin:1.2.5:flatten (execution: flatten, phase: process-resources)
workspace/.metadata/.plugins/org.eclipse.m2e.core/lifecycle-mapping-metadata.xml

<?xml version="1.0" encoding="UTF-8"?>
<lifecycleMappingMetadata>
    <pluginExecutions>
        <pluginExecution>
            <pluginExecutionFilter>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>flatten-maven-plugin</artifactId>
                <goals>
                    <goal>flatten</goal>
                </goals>
                <versionRange>[0.0,)</versionRange>
            </pluginExecutionFilter>
            <action>
                <execute>
                    <runOnIncremental>true</runOnIncremental>
                    <runOnConfiguration>true</runOnConfiguration>
                </execute>
            </action>
        </pluginExecution>
    </pluginExecutions>
</lifecycleMappingMetadata>
```

* Either, consult: [Eclipse documentation](https://www.eclipse.org/m2e/documentation/m2e-execution-not-covered.html)
* or, in Eclipse Luna 4.4.0, you can choose to ignore this error in preferences.
      Window > Preferences > Maven > Errors/Warnings > Plugin execution not covered by lifecycle configuration.
      Select Ignore / Warning / Error as you wish.
