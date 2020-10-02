

package app.coronawarn.server.services.submission.verification;

import feign.Client;

public interface FeignClientProvider {
  Client createFeignClient();
}