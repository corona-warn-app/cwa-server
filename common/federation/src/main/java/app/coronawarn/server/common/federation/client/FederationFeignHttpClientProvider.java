package app.coronawarn.server.common.federation.client;

import feign.Client;

public interface FederationFeignHttpClientProvider {
  Client createFeignClient();
}
