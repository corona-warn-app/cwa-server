package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.INTENSIVE_CARE_CARD;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.ValueTrendCalculator;
import java.util.List;
import java.util.Optional;

public class IntensiveCareCardFactory extends HeaderCardFactory {

  @Override
  protected int getCardId() {
    return INTENSIVE_CARE_CARD.ordinal();
  }

  @Override
  protected KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats, Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(List.of(getOccupiedIntensiveCareBedsReported(stats))).build();
  }

  @Override
  protected List<Optional<Object>> getRequiredFieldValues(StatisticsJsonStringObject stats) {
    List<Optional<Object>> requiredFields = List.of(
        Optional.ofNullable(stats.getOccupiedIntensiveCareBedsReportedDailyRatio()),
        Optional.ofNullable(stats.getOccupiedIntensiveCareBedsReportedDaily1percent())
    );

    if (requiredFields.contains(Optional.empty()) || stats.getOccupiedIntensiveCareBedsReportedDailyRatio() <= 0) {
      return List.of(Optional.empty());
    }

    return requiredFields;
  }

  private KeyFigure getOccupiedIntensiveCareBedsReported(StatisticsJsonStringObject stats) {
    var trend = ValueTrendCalculator.from(stats.getOccupiedIntensiveCareBedsReportedDaily1percent());
    var semantic = ValueTrendCalculator.getNegativeTrendGrowth(trend);

    return KeyFigure.newBuilder()
        .setValue(stats.getOccupiedIntensiveCareBedsReportedDailyRatio())
        .setRank(Rank.PRIMARY)
        .setDecimals(1)
        .setTrend(trend)
        .setTrendSemantic(semantic)
        .build();
  }
}
