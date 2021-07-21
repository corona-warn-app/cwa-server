package app.coronawarn.server.services.distribution.statistics.local;

import static app.coronawarn.server.common.shared.util.TimeUtils.toEpochSecondsUtc;

import app.coronawarn.server.common.protocols.internal.stats.AdministrativeUnitData;
import app.coronawarn.server.common.protocols.internal.stats.FederalStateData;
import app.coronawarn.server.common.protocols.internal.stats.FederalStateData.FederalState;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.LocalStatistics;
import app.coronawarn.server.common.protocols.internal.stats.SevenDayIncidenceData;
import java.time.LocalDate;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class BuildLocalStatisticsHelper {


  /**
   * Supplies with an newly created Local Statistics protobuf containing Federal State statistics.
   *
   * @param federalStateCode - federal state code.
   * @param localStatisticsJsonStringObject - local statistics json object.
   * @return - Local Statistics supplier
   */
  public static Supplier<LocalStatistics> federalStateSupplier(int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return () -> buildFederalStateStatistics(federalStateCode, localStatisticsJsonStringObject);
  }

  /**
   * Enhances an instance of Local Statistics protobuf.
   * Adds Federal State Data to the provided Local Statistics
   *
   * @param federalStateCode - federal state code.
   * @param localStatisticsJsonStringObject - local statistics json object.
   * @return - Function which adds Federal State Data to Local Statistics
   */
  public static Function<LocalStatistics, LocalStatistics> federalStateEnhancer(int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return (localStatistics) -> addFederalStateData(localStatistics, federalStateCode, localStatisticsJsonStringObject);
  }

  /**
   * Supplies with an newly created Local Statistics protobuf containing Administrative Unit statistics.
   *
   * @param federalStateCode - federal state code.
   * @param localStatisticsJsonStringObject - local statistics json object.
   * @return - Local Statistics supplier
   */
  public static Supplier<LocalStatistics> administrativeUnitSupplier(int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return () -> buildAdministrativeUnitStatistics(federalStateCode, localStatisticsJsonStringObject);
  }

  /**
   * Enhances an instance of Local Statistics protobuf.
   * Adds Administrative Unit Data to the provided Local Statistics
   *
   * @param localStatisticsJsonStringObject - local statistics json object.
   * @return - Function which adds Administrative Unit Data to Local Statistics
   */
  public static Function<LocalStatistics, LocalStatistics> administrativeUnitEnhancer(
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return (localStatistics) ->
        addAdministrativeUnitData(localStatistics, localStatisticsJsonStringObject);
  }

  /**
   * Get the federal state index used in configs and protobuf enum {@link FederalState}.
   * It is the real federal state id subtracted by 1.
   * This adjustment took place in order for the federal state to match the exact enum position.
   * Protobuf enums must start from index 0.
   *
   * @param federalStateId - federal state id.
   * @return - federal state index in configurations and protobuf enum.
   */
  public static int getFederalStateConfigIndex(int federalStateId) {
    return federalStateId - 1;
  }

  /**
   * Find trend enum from seven days incidence.
   * INCREASING if trend = 1
   * DECREASING if trend = -1
   * STABLE if trend = 0
   * @param sevenDayIncidence1stReportedTrend1Percent - seven day incidence
   * @return - Trend
   */
  public static Trend findTrendBySevenDayIncidence(int sevenDayIncidence1stReportedTrend1Percent) {
    switch (sevenDayIncidence1stReportedTrend1Percent) {
      case -1:
        return Trend.forNumber(3);
      case 0:
        return Trend.forNumber(1);
      case 1:
        return Trend.forNumber(2);
      default:
        return Trend.UNRECOGNIZED;
    }
  }

  /**
   * Find federal state code from province code.
   * For province codes containing 4 digits, the federal state code consists of first digit.
   * For province codes containing 5 digits, the federal state code consists of first two digits.
   *
   * @param provinceCode - province code.
   * @return - federal state code.
   */
  public static int findFederalStateByProvinceCode(int provinceCode) {
    return provinceCode / 1000;
  }

  /**
   * Build local statistics containing federal state statistics from an instance of
   * {@link LocalStatisticsJsonStringObject}.
   *
   * @param federalStateCode - federal state code.
   * @param localStatisticsJsonStringObject - local statistics json object.
   * @return - Local Statistics protobuf.
   */
  private static LocalStatistics buildFederalStateStatistics(int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return LocalStatistics.newBuilder()
        .addFederalStateData(buildFederalStateData(federalStateCode, localStatisticsJsonStringObject))
        .build();
  }

  /**
   * Build local statistics containing administrative unit statistics from an instance of
   * {@link LocalStatisticsJsonStringObject}.
   *
   * @param federalStateCode - federal state code.
   * @param localStatisticsJsonStringObject - local statistics json object.
   * @return - Local Statistics protobuf.
   */
  private static LocalStatistics buildAdministrativeUnitStatistics(int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return LocalStatistics.newBuilder()
        .addAdministrativeUnitData(buildAdministrativeUnitData(localStatisticsJsonStringObject))
        .build();
  }

  /**
   * Add federal state statistics to an already existing Local Statistics from an instance of
   * {@link LocalStatisticsJsonStringObject}.
   *
   * @param localStatistics - existing local statistics
   * @param federalStateCode - federal state code.
   * @param localStatisticsJsonStringObject - local statistics json object.
   * @return - Local Statistics protobuf containing new federal data.
   */
  private static LocalStatistics addFederalStateData(LocalStatistics localStatistics,
      int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return localStatistics.toBuilder()
        .addFederalStateData(buildFederalStateData(federalStateCode, localStatisticsJsonStringObject))
        .build();
  }

  /**
   * Add administrative unit statistics to an already existing Local Statistics from an instance of
   * {@link LocalStatisticsJsonStringObject}.
   *
   * @param localStatistics - existing local statistics
   * @param localStatisticsJsonStringObject - local statistics json object.
   * @return - Local Statistics protobuf containing new administrative unit data.
   */
  private static LocalStatistics addAdministrativeUnitData(LocalStatistics localStatistics,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return localStatistics.toBuilder()
        .addAdministrativeUnitData(buildAdministrativeUnitData(localStatisticsJsonStringObject))
        .build();
  }


  /**
   * Build Administrative Unit Data from an instance of {@link LocalStatisticsJsonStringObject}.
   * Used for populating Local Statistics.
   * @param localStatisticsJsonStringObject - - local statistics json object.
   * @return - Administrative Unit Data protobuf.
   */
  private static AdministrativeUnitData buildAdministrativeUnitData(
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return AdministrativeUnitData.newBuilder()
        .setAdministrativeUnitShortId(Integer.parseInt(localStatisticsJsonStringObject.getProvinceCode()))
        .setSevenDayIncidence(buildSevenDaysIncidence(localStatisticsJsonStringObject))
        .setUpdatedAt(toEpochSecondsUtc(LocalDate.parse(localStatisticsJsonStringObject.getEffectiveDate())))
        .build();
  }

  /**
   * Build Federal State Data from an instance of {@link LocalStatisticsJsonStringObject}.
   * Used for populating Local Statistics.
   * @param federalStateCode - federal state code.
   * @param localStatisticsJsonStringObject - - local statistics json object.
   * @return - Federal State Data protobuf.
   */
  private static FederalStateData buildFederalStateData(int federalStateCode,
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return FederalStateData.newBuilder()
        .setFederalState(FederalState.forNumber(getFederalStateConfigIndex(federalStateCode)))
        .setSevenDayIncidence(buildSevenDaysIncidence(localStatisticsJsonStringObject))
        .setUpdatedAt(toEpochSecondsUtc(LocalDate.parse(localStatisticsJsonStringObject.getEffectiveDate())))
        .build();
  }

  /**
   * Build Seven Day Incidence Data from an instance of {@link LocalStatisticsJsonStringObject}.
   * Used for populating Local Statistics.
   * @param localStatisticsJsonStringObject - - local statistics json object.
   * @return - Seven Days Incidence Data protobuf.
   */
  private static SevenDayIncidenceData buildSevenDaysIncidence(
      LocalStatisticsJsonStringObject localStatisticsJsonStringObject) {
    return SevenDayIncidenceData.newBuilder()
        .setValue(localStatisticsJsonStringObject.getSevenDayIncidence1stReportedDaily())
        .setTrend(findTrendBySevenDayIncidence(
            localStatisticsJsonStringObject.getSevenDayIncidence1stReportedTrend1Percent()))
        .build();
  }
}
