package app.coronawarn.server.services.federation.upload.keys;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.federation.upload.utils.UploadKeysMockData;

class EfgsUploadKeyFilterTest {

  @Test
  void shouldNotAcceptNonCompliantReportTypeKeys() {
    List<FederationUploadKey> testKeys = List.of(
            UploadKeysMockData.generateRandomUploadKey(ReportType.CONFIRMED_CLINICAL_DIAGNOSIS),
            UploadKeysMockData.generateRandomUploadKey(ReportType.RECURSIVE));

    EfgsUploadKeyFilter keyFilter = new EfgsUploadKeyFilter();
    testKeys.stream().forEach( (uploadKey) -> {
      assertThat(keyFilter.isUploadable(uploadKey)).isFalse();
    });
  }

  @Test
  void shouldAcceptCompliantReportTypeKeys() {
    EfgsUploadKeyFilter keyFilter = new EfgsUploadKeyFilter();
    assertThat(keyFilter
        .isUploadable(UploadKeysMockData.generateRandomUploadKey(ReportType.CONFIRMED_TEST)))
            .isTrue();
  }
}
