package app.coronawarn.server.services.distribution.statistics.local;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.service.LocalStatisticsDownloadService;
import app.coronawarn.server.common.protocols.internal.stats.AdministrativeUnitData;
import app.coronawarn.server.common.protocols.internal.stats.FederalStateData;
import app.coronawarn.server.common.protocols.internal.stats.FederalStateData.FederalState;
import app.coronawarn.server.common.protocols.internal.stats.LocalStatistics;
import app.coronawarn.server.common.shared.util.TimeUtils;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.RegionMappingConfig;
import app.coronawarn.server.services.distribution.statistics.file.MockStatisticJsonFileLoader;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = {DistributionServiceConfig.class, RegionMappingConfig.class})
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"local-json-stats", "processing-test", "debug"})
@ContextConfiguration(classes = {LocalStatisticsJsonProcessingTest.class,
    LocalStatisticsToProtobufMapping.class, MockStatisticJsonFileLoader.class
}, initializers = ConfigDataApplicationContextInitializer.class)
class LocalStatisticsJsonToProtobufTest {

  @Autowired
  LocalStatisticsToProtobufMapping mapping;

  @Autowired
  Map<Integer, LocalStatistics> localStatisticsMap;

  @MockBean
  LocalStatisticsDownloadService service;

  @Test
  void shouldReturnCorrectFederalStateGrouping() {
    assertThat(localStatisticsMap).hasSize(7);

    // first group of federal states consists of 1 state: BW.
    assertThat(localStatisticsMap.get(1).getFederalStateDataCount()).isEqualTo(1);
    assertThat(localStatisticsMap.get(1).getAdministrativeUnitDataCount()).isEqualTo(3);

    // second group of federal states consists of 1 state: BY.
    assertThat(localStatisticsMap.get(2).getFederalStateDataCount()).isEqualTo(1);

    // third group of federal states consists of 3 states: BE, BB, MV.
    assertThat(localStatisticsMap.get(3).getFederalStateDataCount()).isEqualTo(3);
    assertThat(localStatisticsMap.get(3).getAdministrativeUnitDataCount()).isEqualTo(2);

    // fourth group of federal states consists of 4 states: HB, HH, NI, SH.
    assertThat(localStatisticsMap.get(4).getFederalStateDataCount()).isEqualTo(4);

    // fifth group of federal states consists of 1 state: NRW.
    assertThat(localStatisticsMap.get(5).getFederalStateDataCount()).isEqualTo(1);
    assertThat(localStatisticsMap.get(5).getAdministrativeUnitDataCount()).isEqualTo(2);

    // sixth group of federal states consists of 3 state: SN, ST, TH.
    assertThat(localStatisticsMap.get(6).getFederalStateDataCount()).isEqualTo(3);

    // seventh group of federal states consists of 3 state: HE, RP, SL.
    assertThat(localStatisticsMap.get(7).getFederalStateDataCount()).isEqualTo(3);
  }

  @Test
  void shouldContainOnlyMostRecentFederalStateOrAdministrativeUnitStatistics() {

    // Federal state 5 (group 5) is duplicated in JSON sample data. Should return the most recent one.
    assertThat(localStatisticsMap.get(5).getFederalStateData(0))
        .extracting(FederalStateData::getFederalState, FederalStateData::getUpdatedAt)
        .containsExactly(FederalState.FEDERAL_STATE_NRW,
            TimeUtils.toEpochSecondsUtc(LocalDate.of(2021, 5, 18)));

    // Federal state 8 (group 1) is duplicated in JSON sample data. Should return the most recent one.
    assertThat(localStatisticsMap.get(1).getFederalStateData(0))
        .extracting(FederalStateData::getFederalState, FederalStateData::getUpdatedAt)
        .containsExactly(FederalState.FEDERAL_STATE_BW,
            TimeUtils.toEpochSecondsUtc(LocalDate.of(2021, 5, 18)));

    // Administrative Unit 8326 is duplicated in JSON sample data. Should return the most recent one.
    assertThat(localStatisticsMap.get(1).getAdministrativeUnitDataList()
        .stream()
        .filter(administrativeUnitData -> administrativeUnitData.getAdministrativeUnitShortId() == 8326)
        .findFirst().get())
        .extracting(AdministrativeUnitData::getAdministrativeUnitShortId, AdministrativeUnitData::getUpdatedAt)
        .containsExactly(8326,
            TimeUtils.toEpochSecondsUtc(LocalDate.of(2021, 5, 16)));

    // Federal state 12 (group 3) contains data for Seven Day Hospitalization
    assertThat(localStatisticsMap.get(3).getFederalStateData(1))
        .extracting(FederalStateData::getFederalState, FederalStateData::getUpdatedAt,
            FederalStateData::getSevenDayHospitalizationIncidenceUpdatedAt)
        .containsExactly(FederalState.FEDERAL_STATE_BB,
            TimeUtils.toEpochSecondsUtc(LocalDate.of(2021, 5, 17)),
            TimeUtils.toEpochSecondsUtc(LocalDate.of(2021, 5, 16)));
  }


  @Test
  void shouldReturnCorrectNumberOfAdministrativeUnits() {

    // -> 3 administrative units: LK Rottweil, LK Schwarzwald-Baar-Kreis, LK Ortenaukreis.
    assertThat(localStatisticsMap.get(1).getAdministrativeUnitData(0).getAdministrativeUnitShortId()).isEqualTo(8325);
    assertThat(localStatisticsMap.get(1).getAdministrativeUnitData(1).getAdministrativeUnitShortId()).isEqualTo(8326);
    assertThat(localStatisticsMap.get(1).getAdministrativeUnitData(2).getAdministrativeUnitShortId()).isEqualTo(8317);

    // -> 0 administrative units
    assertThat(localStatisticsMap.get(2).getAdministrativeUnitDataCount()).isEqualTo(0);

    // -> 2 administrative units: LK Ostprignitz-Ruppin and SK Berlin Pankow
    assertThat(localStatisticsMap.get(3).getAdministrativeUnitData(0).getAdministrativeUnitShortId()).isEqualTo(12068);
    assertThat(localStatisticsMap.get(3).getAdministrativeUnitData(1).getAdministrativeUnitShortId()).isEqualTo(11003);

    // -> 0 administrative units
    assertThat(localStatisticsMap.get(4).getAdministrativeUnitDataCount()).isEqualTo(0);

    // -> 2 administrative units: LK Märkischer Kreis and SK Oberhausen
    assertThat(localStatisticsMap.get(5).getAdministrativeUnitData(0).getAdministrativeUnitShortId()).isEqualTo(5962);
    assertThat(localStatisticsMap.get(5).getAdministrativeUnitData(1).getAdministrativeUnitShortId()).isEqualTo(5119);

    // -> 2 administrative units: Wartburgkreis and Eisenach
    assertThat(localStatisticsMap.get(6).getAdministrativeUnitData(0).getAdministrativeUnitShortId()).isEqualTo(16063);
    assertThat(localStatisticsMap.get(6).getAdministrativeUnitData(1).getAdministrativeUnitShortId()).isEqualTo(16056);

    // -> 0 administrative units
    assertThat(localStatisticsMap.get(7).getAdministrativeUnitDataCount()).isEqualTo(0);
  }


  @Test
  void shouldCreateEisenAndGiveTheDefaultValuesFromWartburgkreis() {

    AdministrativeUnitData eisenach = localStatisticsMap.get(6).getAdministrativeUnitData(0);
    AdministrativeUnitData wartburgkreis = localStatisticsMap.get(6).getAdministrativeUnitData(1);

    assertThat(eisenach.getSevenDayIncidence().getValue()).isEqualTo(wartburgkreis.getSevenDayIncidence().getValue());
    assertThat(eisenach.getSevenDayIncidence().getTrend()).isEqualTo(wartburgkreis.getSevenDayIncidence().getTrend());

    assertThat(eisenach.getUpdatedAt()).isEqualTo(wartburgkreis.getUpdatedAt());
  }
}
