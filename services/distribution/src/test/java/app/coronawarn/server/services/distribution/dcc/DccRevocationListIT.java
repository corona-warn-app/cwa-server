package app.coronawarn.server.services.distribution.dcc;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dcc.decode.DccRevocationListDecoder;
import app.coronawarn.server.services.distribution.dgc.ApacheHttpTestConfiguration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { DistributionServiceConfig.class,
                                  ProdDccRevocationClient.class,
                                  CloudDccRevocationFeignClientConfiguration.class,
                                  CloudDccRevocationFeignHttpClientProvider.class,
                                  DccRevocationListDecoder.class,
                                  ApacheHttpTestConfiguration.class }, 
                      initializers = ConfigDataApplicationContextInitializer.class)
@ImportAutoConfiguration({ FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class })
@ActiveProfiles({ "dsc-rev-client-factory", "revocation" })
class DccRevocationListIT {

  @Autowired
  private DccRevocationClient dccRevocationClient;

  @Test
  void should_fetch_dcc_revocation_list() throws FetchDccListException {
    Optional<List<RevocationEntry>> revocationEntryList = dccRevocationClient.getDccRevocationList();
    assertThat(revocationEntryList).isPresent();
  }

  @Test
  void should_fetch_etag() throws FetchDccListException {
    final String etag = dccRevocationClient.getETag();
    assertThat(etag).isNotBlank();
    assertThat(etag).doesNotContain("test");
  }
}
