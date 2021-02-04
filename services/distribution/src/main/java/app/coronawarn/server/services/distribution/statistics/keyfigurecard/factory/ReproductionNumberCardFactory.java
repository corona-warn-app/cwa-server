package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.ValueTrendCalculator;
import java.util.List;
import java.util.Optional;

public class ReproductionNumberCardFactory extends HeaderCardFactory {

  @Override
  protected Integer getCardId() {
    return KeyFigureCardSequenceConstants.REPRODUCTION_NUMBER_CARD;
  }

  @Override
  protected KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats, Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(List.of(
        getSevenDayRValueKeyFigure(stats))).build();
  }

  private KeyFigure getSevenDayRValueKeyFigure(StatisticsJsonStringObject stats) {
    var trend = ValueTrendCalculator.from(stats.getSevenDayRvaluePublishedTrend1percent());
    var semantic = ValueTrendCalculator.getNegativeTrendGrowth(trend);
    return KeyFigure.newBuilder()
        .setValue(stats.getSevenDayRvaluePublishedDaily())
        .setRank(Rank.PRIMARY)
        .setDecimals(2)
        .setTrend(trend)
        .setTrendSemantic(semantic)
        .build();
  }

  @Override
  protected List<Optional<Object>> getRequiredFieldValues(StatisticsJsonStringObject stats) {
    List<Optional<Object>> requiredFields = List.of(
        Optional.ofNullable(stats.getSevenDayRvaluePublishedTrend1percent()),
        Optional.ofNullable(stats.getSevenDayRvaluePublishedDaily())
    );

    if (requiredFields.contains(Optional.empty()) || stats.getSevenDayRvaluePublishedDaily() <= 0) {
      return List.of(Optional.empty());
    }

    return requiredFields;
  }
}
