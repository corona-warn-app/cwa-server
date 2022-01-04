package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.HOSPITALIZATION_INCIDENCE_CARD;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.ValueTrendCalculator;
import java.util.List;
import java.util.Optional;

public class HospitalizationIncidenceCardFactory extends HeaderCardFactory {

  @Override
  protected int getCardId() {
    return HOSPITALIZATION_INCIDENCE_CARD.ordinal();
  }

  @Override
  protected KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats, Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(List.of(getSevenDayHospitalizationReport(stats))).build();
  }

  @Override
  protected List<Optional<Object>> getRequiredFieldValues(StatisticsJsonStringObject stats) {
    List<Optional<Object>> requiredFields = List.of(
        Optional.ofNullable(stats.getSevenDayHospitalizationReportedTrend1percent()),
        Optional.ofNullable(stats.getSevenDayHospitalizationReportedDaily())
    );

    if (requiredFields.contains(Optional.empty()) || stats.getSevenDayHospitalizationReportedDaily() <= 0) {
      return List.of(Optional.empty());
    }

    return requiredFields;
  }

  private KeyFigure getSevenDayHospitalizationReport(StatisticsJsonStringObject stats) {
    var trend = ValueTrendCalculator.from(stats.getSevenDayHospitalizationReportedTrend1percent());
    var semantic = ValueTrendCalculator.getNegativeTrendGrowth(trend);

    return KeyFigure.newBuilder()
        .setValue(stats.getSevenDayHospitalizationReportedDaily())
        .setRank(Rank.PRIMARY)
        .setDecimals(1)
        .setTrend(trend)
        .setTrendSemantic(semantic)
        .build();
  }
}
