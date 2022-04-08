package app.coronawarn.server.services.distribution.dcc;


import com.google.protobuf.ByteString;
import io.micrometer.core.annotation.Timed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "dcc-rev-server", configuration = CloudDccRevocationFeignClientConfiguration.class,
    url = "${services.distribution.dcc-revocation.client.base-url}")
public interface DccRevocationFeignClient {

  @Timed
  @GetMapping(value = "${services.distribution.dcc-revocation.dcc-list-path}")
  ResponseEntity<byte[]> getRevocationList();
}
