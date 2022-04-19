package app.coronawarn.server.services.distribution.dcc;

import feign.Client;

public interface DccRevocationFeignHttpClientProvider {
  Client createDccRevocationFeignClient();
}
