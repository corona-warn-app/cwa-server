package app.coronawarn.server.services.federation.download.download;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "federation-server", url = "${services.federation-download.federation-gateway.base-url}")
public interface FederationGatewayClient {

  @GetMapping(value = "${services.federation-download.federation-gateway.path}" + "/{date}")
  String getDiagnosisKeys(@RequestHeader("Accept") String accept,
      @RequestHeader("X-SSL-Client-SHA256") String shaClient, @RequestHeader("X-SSL-Client-DN") String dnClient,
      @PathVariable("date") String date);
}
