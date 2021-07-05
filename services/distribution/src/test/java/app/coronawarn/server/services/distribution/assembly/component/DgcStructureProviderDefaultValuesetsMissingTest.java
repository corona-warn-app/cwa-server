package app.coronawarn.server.services.distribution.assembly.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToCborMapping;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = {DistributionServiceConfig.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {DigitalGreenCertificateToProtobufMapping.class, CryptoProvider.class,
        DistributionServiceConfig.class},
    initializers = ConfigDataApplicationContextInitializer.class)
class DgcStructureProviderDefaultValuesetsMissingTest {

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  CryptoProvider cryptoProvider;

  @MockBean
  DigitalGreenCertificateToProtobufMapping dgcToProtobufMappingMock;

  @MockBean
  DigitalGreenCertificateToCborMapping dgcToCborMappingMock;

  @Test
  void default_value_missing_should_result_in_empty_dir() throws UnableToLoadFileException {
    DigitalGreenCertificateStructureProvider underTest = new DigitalGreenCertificateStructureProvider(
        distributionServiceConfig, cryptoProvider, dgcToProtobufMappingMock, dgcToCborMappingMock);
    when(dgcToProtobufMappingMock.constructProtobufMapping())
        .thenThrow(new UnableToLoadFileException(""));
    DirectoryOnDisk digitalGreenCertificates = underTest.getDigitalGreenCertificates();
    digitalGreenCertificates.prepare(new ImmutableStack<>());

    assertEquals("", digitalGreenCertificates.getName());
  }
}
