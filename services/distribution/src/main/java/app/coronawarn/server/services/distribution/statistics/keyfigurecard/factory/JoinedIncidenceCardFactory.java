package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.JOINED_INCIDENCE_CARD;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.ValueTrendCalculator;
import java.util.List;
import java.util.Optional;

public class JoinedIncidenceCardFactory extends HeaderCardFactory {

  @Override
  protected int getCardId() {
    return JOINED_INCIDENCE_CARD.ordinal();
  }

  @Override
  protected KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats, Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(List.of(
        getIncidencePublishdDaily(stats),
        getHospitalizationReportedDaily(stats))).build();
  }

  private KeyFigure getIncidencePublishdDaily(StatisticsJsonStringObject stats) {
    var trend = ValueTrendCalculator.from(stats.getSevenDayIncidenceTrend1percent());
    var semantic = ValueTrendCalculator.getNegativeTrendGrowth(trend);

    return KeyFigure.newBuilder()
        .setValue(stats.getSevenDayIncidence())
        .setRank(Rank.PRIMARY)
        .setTrend(trend)
        .setTrendSemantic(semantic)
        .setDecimals(1)
        .build();
  }

  private KeyFigure getHospitalizationReportedDaily(StatisticsJsonStringObject stats) {
    var trend = ValueTrendCalculator.from(stats.getSevenDayHospitalizationReportedTrend1percent());
    var semantic = ValueTrendCalculator.getNegativeTrendGrowth(trend);

    return KeyFigure.newBuilder()
        .setValue(stats.getSevenDayHospitalizationReportedDaily())
        .setRank(Rank.SECONDARY)
        .setTrend(trend)
        .setTrendSemantic(semantic)
        .setDecimals(1)
        .build();
  }


  @Override
  protected List<Optional<Object>> getRequiredFieldValues(StatisticsJsonStringObject stats) {
    List<Optional<Object>> requiredFields = List.of(
        Optional.ofNullable(stats.getSevenDayIncidence()),
        Optional.ofNullable(stats.getSevenDayHospitalizationReportedDaily()));

    if (requiredFields.contains(Optional.empty())
        || stats.getSevenDayIncidence() <= 0
        || stats.getSevenDayHospitalizationReportedDaily() <= 0) {
      return List.of(Optional.empty());
    }

    return requiredFields;
  }
}
