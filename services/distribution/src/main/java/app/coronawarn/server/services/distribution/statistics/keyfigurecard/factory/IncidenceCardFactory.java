package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.ValueTrendCalculator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class IncidenceCardFactory extends HeaderCardFactory {

  @Override
  protected Integer getCardId() {
    return KeyFigureCardSequenceConstants.INCIDENCE_CARD_ID;
  }

  private KeyFigure getIncidence(StatisticsJsonStringObject stats) {
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

  @Override
  protected KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats, Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(
        List.of(this.getIncidence(stats)))
        .build();
  }

  @Override
  protected List<Optional<Object>> getRequiredFieldValues(StatisticsJsonStringObject stats) {
    List<Optional<Object>> requiredFields = List.of(
        Optional.ofNullable(stats.getSevenDayIncidenceTrend1percent()),
        Optional.ofNullable(stats.getSevenDayIncidence())
    );

    if (requiredFields.contains(Optional.empty()) || stats.getSevenDayIncidence() <= 0) {
      return Collections.emptyList();
    }

    return requiredFields;
  }
}
