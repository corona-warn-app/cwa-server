package app.coronawarn.server.services.distribution.assembly.component;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;

import app.coronawarn.server.common.persistence.service.DccRevocationListService;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dcc.DccRevocationListToProtobufMapping;
import app.coronawarn.server.services.distribution.dcc.FetchDccListException;
import app.coronawarn.server.services.distribution.dcc.TestDccRevocationClient;
import app.coronawarn.server.services.distribution.dcc.decode.DccRevocationListDecoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = { DistributionServiceConfig.class })
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { DccRevocationListStructureProvider.class,
    CryptoProvider.class, DistributionServiceConfig.class,
    TestDccRevocationClient.class,
    DccRevocationListToProtobufMapping.class,
    DccRevocationListDecoder.class,
}, initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles({ "fake-dcc-revocation", "revocation" })
class DccRevocationListStructureProviderExceptionTests {

  @MockBean
  DccRevocationListService dccRevocationListService;

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @MockBean
  DccRevocationListToProtobufMapping dccRevocationListToProtobufMapping;

  @MockBean
  TestDccRevocationClient dccRevocationClient;

  @MockBean
  OutputDirectoryProvider outputDirectoryProvider;

  @Autowired
  DccRevocationListStructureProvider underTest;

  @Test
  void coverFetchDccRevocationListFetchDccListException() throws Exception {
    doThrow(FetchDccListException.class).when(dccRevocationClient).getETag();
    underTest.fetchDccRevocationList();
    assertThatThrownBy(() -> dccRevocationClient.getETag())
        .isExactlyInstanceOf(FetchDccListException.class);
  }

  @Test
  void coverProtobufMappingChunkList() throws Exception {
    doThrow(RuntimeException.class).when(dccRevocationListToProtobufMapping)
        .constructProtobufMappingChunkList(anyList());
    underTest.getDccRevocationKidTypeChunk(emptyList());
    assertThatThrownBy(() -> dccRevocationListToProtobufMapping.constructProtobufMappingChunkList(anyList()))
        .isExactlyInstanceOf(RuntimeException.class);

  }

  @Test
  void coverProtobufMappingKidList() throws Exception {
    doThrow(RuntimeException.class).when(dccRevocationListToProtobufMapping).constructProtobufMappingKidList(anyMap());
    underTest.getDccRevocationDirectory();
    assertThatThrownBy(() -> dccRevocationListToProtobufMapping.constructProtobufMappingKidList(anyMap()))
        .isExactlyInstanceOf(RuntimeException.class);
  }

  @Test
  void coverProtobufMappingKidType() throws Exception {
    doThrow(RuntimeException.class).when(dccRevocationListToProtobufMapping).constructProtobufMappingKidType(anyList());
    underTest.getDccRevocationKidTypeArchive(emptyList());
    assertThatThrownBy(() -> dccRevocationListToProtobufMapping.constructProtobufMappingKidType(anyList()))
        .isExactlyInstanceOf(RuntimeException.class);
  }
}
