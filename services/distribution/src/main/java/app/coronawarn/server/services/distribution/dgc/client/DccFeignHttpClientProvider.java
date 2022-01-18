package app.coronawarn.server.services.distribution.dgc.client;

import feign.Client;

public interface DccFeignHttpClientProvider {

  Client createFeignClient();
}
