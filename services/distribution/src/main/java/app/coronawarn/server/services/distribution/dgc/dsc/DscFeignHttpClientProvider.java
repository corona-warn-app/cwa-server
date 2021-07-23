package app.coronawarn.server.services.distribution.dgc.dsc;

import feign.Client;

public interface DscFeignHttpClientProvider {
  Client createDscFeignClient();
}
