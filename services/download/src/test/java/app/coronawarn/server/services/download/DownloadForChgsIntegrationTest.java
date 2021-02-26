package app.coronawarn.server.services.download;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.FederationBatchInfo;
import app.coronawarn.server.common.persistence.domain.FederationBatchStatus;
import app.coronawarn.server.common.persistence.domain.FederationBatchTarget;
import app.coronawarn.server.common.persistence.repository.FederationBatchInfoRepository;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"connect-chgs"})
public class DownloadForChgsIntegrationTest extends GatewayServiceIntegrationSuite {

  @Autowired
  private FederationBatchInfoRepository batchInfoRepository;


  @ParameterizedTest
  @EnumSource(value = FederationBatchTarget.class, names = {"CHGS"})
  void downloadShouldRunSuccessfulFor(FederationBatchTarget target) {
    final List<FederationBatchInfo> processedBatches = batchInfoRepository
        .findByStatus(FederationBatchStatus.PROCESSED.name());
    assertThat(batchInfoRepository.findAll()).hasSize(2);
    assertThat(processedBatches).extracting(FederationBatchInfo::getTargetSystem).containsExactly(
        target);

  }
}
