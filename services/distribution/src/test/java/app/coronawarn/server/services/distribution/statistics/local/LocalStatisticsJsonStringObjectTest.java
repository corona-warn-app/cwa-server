package app.coronawarn.server.services.distribution.statistics.local;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LocalStatisticsJsonStringObjectTest {

  @Test
  void testGetSevenDayIncidence1stReportedDaily() {
    LocalStatisticsJsonStringObject object = new LocalStatisticsJsonStringObject();
    assertEquals(Double.valueOf(0.0), object.getSevenDayIncidence1stReportedDaily());

    object.setSevenDayIncidence1stReportedDaily(42.42);
    assertEquals(42.42, object.getSevenDayIncidence1stReportedDaily());
  }

  @Test
  void testGetSevenDayIncidence1stReportedTrend1Percent() {
    LocalStatisticsJsonStringObject object = new LocalStatisticsJsonStringObject();
    assertEquals(Integer.valueOf(0), object.getSevenDayIncidence1stReportedTrend1Percent());

    object.setSevenDayIncidence1stReportedTrend1Percent(42);
    assertEquals(42, object.getSevenDayIncidence1stReportedTrend1Percent());
  }

  @Test
  void testIsComplete() {
    LocalStatisticsJsonStringObject object = new LocalStatisticsJsonStringObject();
    assertFalse(object.isComplete());

    object.setSevenDayIncidence1stReportedDaily(42.42);
    assertFalse(object.isComplete());

    object.setSevenDayIncidence1stReportedDaily(null);
    object.setSevenDayIncidence1stReportedTrend1Percent(42);
    assertFalse(object.isComplete());

    object.setSevenDayIncidence1stReportedDaily(42.42);
    assertTrue(object.isComplete());
  }
}
