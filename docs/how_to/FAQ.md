# FAQ & Troubleshooting

This document gathers common problems and pitfalls, as well as possible solutions.

#####<u>P: The debugger does not attach to services / The debugger does not stop at breakpoints.</u>

Try adding `-Dspring-boot.run.fork=false` to the services run config. E.g.: `spring-boot:run -Dspring-boot.run.fork=false`.

#####<u>P: Some Classes, Fields or Methods are (suddenly) undefined.</u>

Try rebuilding the maven persistence package either by using ``mvn clean install`` in the root folder or in `<root>/common/persistence`.

#####<u>Headline</u>



#####<u>Headline</u>
