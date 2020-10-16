

package app.coronawarn.server.services.submission.controller;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import org.assertj.core.util.Lists;
import org.junit.jupiter.params.provider.Arguments;
import java.util.List;
import java.util.stream.Stream;
import static app.coronawarn.server.services.submission.controller.SubmissionPayloadMockData.*;

public class TEKDatasetGeneration {

  private static Stream<Arguments> getOverlappingTestDatasets() {
    return Stream.of(
        getOverlappingDatasetWithFixedPeriods(),
        getOverlappingDatasetWithFlexiblePeriods(),
        getMixedOverlappingDataset()
    ).map(Arguments::of);
  }

  private static List<TemporaryExposureKey> getOverlappingDatasetWithFixedPeriods(){
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(6);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + (DiagnosisKey.MAX_ROLLING_PERIOD / 2);
    return Lists.list(
        buildTemporaryExposureKey(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 1, ReportType.CONFIRMED_TEST,1),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber2, 3, ReportType.CONFIRMED_TEST,1));

  }

  private static List<TemporaryExposureKey> getOverlappingDatasetWithFlexiblePeriods(){
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(6);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + (DiagnosisKey.MAX_ROLLING_PERIOD / 2);
    return Lists.list(
        buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 1, 54),
        buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 1, 90),
        buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_2, rollingStartIntervalNumber2, 3, 133));

  }

  private static List<TemporaryExposureKey> getMixedOverlappingDataset(){
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(6);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + (DiagnosisKey.MAX_ROLLING_PERIOD / 2);
    return Lists.list(
        buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 1, 54),
        buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1, rollingStartIntervalNumber1, 1, 90),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber2, 2, ReportType.CONFIRMED_TEST,1));

  }

  private static Stream<Arguments> getRollingPeriodDatasets() {
    return Stream.of(
        getFixedRollingPeriodDataset(),
        getFlexibleRollingPeriodDataset(),
        getMixedRollingPeriodDataset()
    ).map(Arguments::of);
  }

  private static List<TemporaryExposureKey> getFixedRollingPeriodDataset(){
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(6);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.MAX_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + DiagnosisKey.MAX_ROLLING_PERIOD;
    return Lists.list(
        buildTemporaryExposureKey(VALID_KEY_DATA_3, rollingStartIntervalNumber1, 3, ReportType.CONFIRMED_TEST,1),
        buildTemporaryExposureKey(VALID_KEY_DATA_3, rollingStartIntervalNumber3, 3, ReportType.CONFIRMED_TEST,1),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber2, 3, ReportType.CONFIRMED_TEST,1));
  }

  private static List<TemporaryExposureKey> getFlexibleRollingPeriodDataset(){
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(6);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.MAX_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + DiagnosisKey.MAX_ROLLING_PERIOD;
    return Lists.list(
        buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1, rollingStartIntervalNumber1,3, 54),
        buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1, rollingStartIntervalNumber1,3, 90),
        buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_3, rollingStartIntervalNumber3,3, 133),
        buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_3, rollingStartIntervalNumber3,1, 11),
        buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_2, rollingStartIntervalNumber2,1, 100));
  }

  private static List<TemporaryExposureKey> getMixedRollingPeriodDataset(){
    int rollingStartIntervalNumber1 = createRollingStartIntervalNumber(6);
    int rollingStartIntervalNumber2 = rollingStartIntervalNumber1 + DiagnosisKey.MAX_ROLLING_PERIOD;
    int rollingStartIntervalNumber3 = rollingStartIntervalNumber2 + DiagnosisKey.MAX_ROLLING_PERIOD;
    return Lists.list(
        buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1, rollingStartIntervalNumber1,3, 54),
        buildTemporaryExposureKeyWithFlexibleRollingPeriod(VALID_KEY_DATA_1, rollingStartIntervalNumber1,3, 90),
        buildTemporaryExposureKey(VALID_KEY_DATA_3, rollingStartIntervalNumber3, 3, ReportType.CONFIRMED_TEST,1),
        buildTemporaryExposureKey(VALID_KEY_DATA_2, rollingStartIntervalNumber2, 3, ReportType.CONFIRMED_TEST,1));
  }

}
