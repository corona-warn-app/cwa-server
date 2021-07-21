package app.coronawarn.server.services.distribution.statistics.local;

import static app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend.DECREASING;
import static app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend.INCREASING;
import static app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend.STABLE;
import static app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend.UNRECOGNIZED;
import static app.coronawarn.server.services.distribution.statistics.local.BuildLocalStatisticsHelper.findFederalStateByProvinceCode;
import static app.coronawarn.server.services.distribution.statistics.local.BuildLocalStatisticsHelper.findTrendBySevenDayIncidence;
import static app.coronawarn.server.services.distribution.statistics.local.BuildLocalStatisticsHelper.getFederalStateConfigIndex;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BuildLocalStatisticsHelperTest {

  @ParameterizedTest
  @ValueSource(ints = {-1, 0, 1, 2, 3, 5, 7, 11})
  void testGetFederalStateConfigIndex(int i) {
    assertEquals(i, getFederalStateConfigIndex(i + 1));
  }

  @ParameterizedTest
  @ValueSource(ints = {-2, 2, 3, 5, 7, 11})
  void testFindTrendBySevenDayIncidenceUnrecognized(int i) {
    assertEquals(UNRECOGNIZED, findTrendBySevenDayIncidence(i));
  }

  @Test
  void testFindTrendBySevenDayIncidence() {
    assertEquals(DECREASING, findTrendBySevenDayIncidence(-1));
    assertEquals(STABLE, findTrendBySevenDayIncidence(0));
    assertEquals(INCREASING, findTrendBySevenDayIncidence(1));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1111, 0, 1111, 2222, 3333, 5555, 7777})
  void testFindFederalStateByProvinceCode(int i) {
    assertEquals(i / 1000, findFederalStateByProvinceCode(i));
  }
}
