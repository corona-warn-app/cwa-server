package app.coronawarn.server.services.distribution.dcc;

import io.micrometer.core.annotation.Timed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "dcc-rev-server",
             configuration = CloudDccRevocationFeignClientConfiguration.class, 
             url = "${services.distribution.dcc-revocation.client.base-url}")
public interface DccRevocationFeignClient {

  @Timed
  @GetMapping(path = "${services.distribution.dcc-revocation.dcc-list-path}")
  ResponseEntity<byte[]> getRevocationList();

  @Timed
  @RequestMapping(method = RequestMethod.HEAD, path = "${services.distribution.dcc-revocation.dcc-list-path}")
  ResponseEntity<Void> head();
}
