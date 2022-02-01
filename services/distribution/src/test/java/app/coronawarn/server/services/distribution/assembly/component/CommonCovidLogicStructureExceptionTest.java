package app.coronawarn.server.services.distribution.assembly.component;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@EnableConfigurationProperties(value = {DistributionServiceConfig.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {CommonCovidLogicStructureProvider.class, CommonCovidLogicArchiveBuilder.class,
        CryptoProvider.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles({"fake-dcc-client", "fake-dsc-client"})
class CommonCovidLogicStructureExceptionTest {

  @MockBean
  DigitalCovidCertificateClient digitalCovidCertificateClient;

  @Autowired
  CommonCovidLogicArchiveBuilder commonCovidLogicArchiveBuilder;

  @Autowired
  CryptoProvider cryptoProvider;
  @Autowired
  CommonCovidLogicStructureProvider underTest;

  @Test
  void shouldContainEmptyOptionalWhenThrowFetchBusinessRulesException() throws FetchBusinessRulesException {
    when(digitalCovidCertificateClient.getCommonCovidLogicRules()).thenThrow(FetchBusinessRulesException.class);
    Optional<Writable<WritableOnDisk>> commonCovidLogicRules = underTest.getCommonCovidLogicRules();

    assertTrue(commonCovidLogicRules.isEmpty());
  }
}
