package app.coronawarn.server.services.distribution.statistics.local;

import static app.coronawarn.server.common.shared.util.TimeUtils.toEpochSecondsUTC;

import app.coronawarn.server.common.protocols.internal.stats.AdministrativeUnitData;
import app.coronawarn.server.common.protocols.internal.stats.FederalStateData;
import app.coronawarn.server.common.protocols.internal.stats.FederalStateData.FederalState;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.LocalStatistics;
import app.coronawarn.server.common.protocols.internal.stats.SevenDayIncidenceData;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class BuildLocalStatisticsHelper {

  public static int getFederalStateConfigIndex(Integer federalStateId) {
    return federalStateId - 1;
  }

  public static Trend findTrendBySevenDayIncidence(int sevenDayIncidence1stReportedTrend1Percent) {
    switch (sevenDayIncidence1stReportedTrend1Percent) {
      case -1:
        return Trend.forNumber(3);
      case 0:
        return Trend.forNumber(1);
      case 1:
        return Trend.forNumber(2);
      default:
        return Trend.forNumber(-1);
    }
  }

  public static Optional<String> findFederalStateByProvinceCode(String provinceCode) {
    switch (provinceCode.length()) {
      case 1:
      case 2:
        return Optional.of(provinceCode);
      case 4:
        return Optional.of(provinceCode.substring(0, 1));
      case 5:
        return Optional.of(provinceCode.substring(0, 2));
      default:
        return Optional.empty();
    }
  }

  public static LocalStatistics buildFederalStateStatistics(int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return LocalStatistics.newBuilder()
        .addFederalStateData(buildFederalStateData(federalStateCode, localStatisticsJsonStringObject))
        .build();
  }

  public static LocalStatistics buildAdministrativeUnitStatistics(int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return LocalStatistics.newBuilder()
        .addAdministrativeUnitData(buildAdministrativeUnitData(localStatisticsJsonStringObject))
        .build();
  }

  public static LocalStatistics addAdministrativeUnitData(LocalStatistics localStatistics,
      int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return localStatistics.toBuilder()
        .addAdministrativeUnitData(buildAdministrativeUnitData(localStatisticsJsonStringObject))
        .build();
  }

  public static LocalStatistics addFederalStateData(LocalStatistics localStatistics,
      int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return localStatistics.toBuilder()
        .addFederalStateData(buildFederalStateData(federalStateCode, localStatisticsJsonStringObject))
        .build();
  }

  public static Supplier<LocalStatistics> federalStateSupplier(int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return () -> buildFederalStateStatistics(federalStateCode, localStatisticsJsonStringObject);
  }

  public static Function<LocalStatistics, LocalStatistics> federalStateEnhancer(int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return (localStatistics) -> addFederalStateData(localStatistics, federalStateCode, localStatisticsJsonStringObject);
  }

  public static Supplier<LocalStatistics> administrativeUnitSupplier(int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return () -> buildAdministrativeUnitStatistics(federalStateCode, localStatisticsJsonStringObject);
  }

  public static Function<LocalStatistics, LocalStatistics> administrativeUnitEnhancer(int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return (localStatistics) -> addAdministrativeUnitData(localStatistics, federalStateCode, localStatisticsJsonStringObject);
  }

  private static AdministrativeUnitData buildAdministrativeUnitData(LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return AdministrativeUnitData.newBuilder()
        .setAdministrativeUnitShortId(Integer.parseInt(localStatisticsJsonStringObject.getProvinceCode()))
        .setSevenDayIncidence(buildSevenDaysIncidence(localStatisticsJsonStringObject))
        .setUpdatedAt(toEpochSecondsUTC(LocalDate.parse(localStatisticsJsonStringObject.getEffectiveDate())))
        .build();
  }

  private static FederalStateData buildFederalStateData(int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return FederalStateData.newBuilder()
        .setFederalState(FederalState.forNumber(getFederalStateConfigIndex(federalStateCode)))
        .setSevenDayIncidence(buildSevenDaysIncidence(localStatisticsJsonStringObject))
        .build();
  }

  private static SevenDayIncidenceData buildSevenDaysIncidence(
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return SevenDayIncidenceData.newBuilder()
        .setValue(localStatisticsJsonStringObject.getSevenDayIncidence1stReportedDaily())
        .setTrend(findTrendBySevenDayIncidence(
            localStatisticsJsonStringObject.getSevenDayIncidence1stReportedTrend1Percent()))
        .build();
  }
}
