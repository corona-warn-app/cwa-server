package app.coronawarn.server.common.federation.client;

import feign.Client;

public interface FeignClientProvider {
  Client createFeignClient();
}
