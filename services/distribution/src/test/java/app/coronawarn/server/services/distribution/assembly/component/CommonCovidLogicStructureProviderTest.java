package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToCborMapping;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.dsc.DigitalSigningCertificatesClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


@EnableConfigurationProperties(value = {DistributionServiceConfig.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {DigitalGreenCertificateToCborMapping.class,
        CryptoProvider.class, DistributionServiceConfig.class,
        DigitalSigningCertificatesClient.class, TestDigitalCovidCertificateClient.class,
        CommonCovidLogicStructureProvider.class, CommonCovidLogicArchiveBuilder.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles({"fake-dcc-client", "fake-dsc-client"})
class CommonCovidLogicStructureProviderTest {

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  CryptoProvider cryptoProvider;

  @Autowired
  DigitalGreenCertificateToCborMapping dgcToCborMappingMock;

  @Autowired
  TestDigitalCovidCertificateClient digitalCovidCertificateClient;

  @MockBean
  DigitalSigningCertificatesClient digitalSigningCertificatesClient;

  @Autowired
  CommonCovidLogicStructureProvider underTest;

  @Test
  void shouldCreateCorrectFileStructureForCommonCovidLogicRules() {
    Optional<Writable<WritableOnDisk>> commonCovidLogicRules = underTest.getCommonCovidLogicRules();
    Collection<String> archiveContent;

    commonCovidLogicRules.get().prepare(new ImmutableStack<>());
    archiveContent = ((DirectoryOnDisk) commonCovidLogicRules.get()).getWritables().stream()
        .map(Writable::getName).collect(Collectors.toList());

    assertEquals("ccl", commonCovidLogicRules.get().getName());
    assertThat(archiveContent).containsAll(Set.of("config-v1", "config-v2"));
  }

}
