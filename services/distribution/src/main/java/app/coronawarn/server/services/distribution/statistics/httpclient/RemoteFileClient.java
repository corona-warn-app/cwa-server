package app.coronawarn.server.services.distribution.statistics.httpclient;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "remote-file-client", url = "${remote-file-client.base-url}")
public interface RemoteFileClient {

  @RequestMapping(method = RequestMethod.GET, value = "/json/v1/cwa_reporting_public_data.json",
    headers = {
      "X-SSL-Client-SHA256=${remote-file-client.auth.access_key}"
    })
  Response downloadStatisticsFile();

}
