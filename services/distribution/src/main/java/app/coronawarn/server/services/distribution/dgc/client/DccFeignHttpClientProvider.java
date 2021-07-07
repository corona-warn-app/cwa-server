package app.coronawarn.server.services.distribution.dgc.client;

import feign.Client;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;


public interface DccFeignHttpClientProvider {
  Client createFeignClient();
}
