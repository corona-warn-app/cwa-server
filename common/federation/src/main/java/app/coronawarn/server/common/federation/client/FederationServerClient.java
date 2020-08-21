package app.coronawarn.server.common.federation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * This is a Spring Cloud Feign based HTTP client that allows type-safe HTTP calls and abstract the implementation away.
 * Add @EnableFeignClients({"app.coronawarn.server.common.federation"}) in your spring boot application to use this
 * client.
 */
@FeignClient(name = "federation-server", configuration = FederationServerClientConfiguration.class,
    url = "${services.common.federation-gateway.base-url}")
public interface FederationServerClient {

  //TODO:: adapt method to return a list of diagnosis keys
  //TODO:: refactor somehow to use less method parameters
  @GetMapping(value = "${services.common.federation-gateway.batch-download-url}"
      + "/{date}")
  String getDiagnosisKeys(@RequestHeader("Accept") String accept,
      @RequestHeader("X-SSL-Client-SHA256") String shaClient,
      @RequestHeader("X-SSL-Client-DN") String dnClient,
      @PathVariable String date);
}
