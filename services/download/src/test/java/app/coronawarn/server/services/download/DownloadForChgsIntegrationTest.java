package app.coronawarn.server.services.download;

import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.domain.FederationBatchSourceSystem;
import app.coronawarn.server.common.persistence.repository.FederationBatchInfoRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"connect-chgs", "enable-date-based-download"})
public class DownloadForChgsIntegrationTest extends GatewayServiceIntegrationSuite {

  @Autowired
  private FederationBatchInfoRepository batchInfoRepository;


  @ParameterizedTest
  @EnumSource(value = FederationBatchSourceSystem.class, names = {"CHGS"})
  void downloadShouldRunSuccessfulFor(FederationBatchSourceSystem source) {
    final List<FederationBatchInfo> processedBatches = batchInfoRepository
        .findByStatus(FederationBatchStatus.PROCESSED.name());
    assertThat(batchInfoRepository.findAll()).hasSize(2);
    assertThat(processedBatches).extracting(FederationBatchInfo::getSourceSystem).containsExactly(
        source);

  }
}
