package app.coronawarn.server.common.federation.client;

import feign.Client;

public interface FeignFederationHttpClientProvider {
  Client createFeignClient();
}
