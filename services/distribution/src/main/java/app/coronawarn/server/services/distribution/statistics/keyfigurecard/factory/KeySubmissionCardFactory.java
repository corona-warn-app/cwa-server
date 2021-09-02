package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.KEY_SUBMISSION_CARD;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.ValueTrendCalculator;
import java.util.List;
import java.util.Optional;


public class KeySubmissionCardFactory extends HeaderCardFactory {

  @Override
  protected int getCardId() {
    return KEY_SUBMISSION_CARD.ordinal();
  }

  private KeyFigure getPersonWhoSharedKeysDaily(StatisticsJsonStringObject stats) {
    return KeyFigure.newBuilder()
        .setValue(stats.getPersonsWhoSharedKeysDaily())
        .setRank(Rank.PRIMARY)
        .setDecimals(0)
        .setTrend(Trend.UNSPECIFIED_TREND)
        .setTrendSemantic(TrendSemantic.UNSPECIFIED_TREND_SEMANTIC)
        .build();
  }

  private KeyFigure getPersonWhoSharedKeysCumulated(StatisticsJsonStringObject stats) {
    return KeyFigure.newBuilder()
        .setValue(stats.getPersonsWhoSharedKeysCumulated())
        .setRank(Rank.TERTIARY)
        .setDecimals(0)
        .setTrend(Trend.UNSPECIFIED_TREND)
        .setTrendSemantic(TrendSemantic.UNSPECIFIED_TREND_SEMANTIC)
        .build();
  }

  private KeyFigure getPersonWhoSharedKeysSum(StatisticsJsonStringObject stats) {
    var trend = ValueTrendCalculator.from(stats.getPersonsWhoSharedKeys7daysTrend5percent());
    return KeyFigure.newBuilder()
        .setValue(stats.getPersonWhoSharedKeys7daysAvg())
        .setRank(Rank.SECONDARY)
        .setDecimals(0)
        .setTrend(trend)
        .setTrendSemantic(TrendSemantic.NEUTRAL)
        .build();
  }

  @Override
  protected KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats, Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(
        List.of(
            getPersonWhoSharedKeysDaily(stats),
            getPersonWhoSharedKeysSum(stats),
            getPersonWhoSharedKeysCumulated(stats)))
        .build();
  }

  @Override
  protected List<Optional<Object>> getRequiredFieldValues(StatisticsJsonStringObject stats) {
    List<Optional<Object>> requiredFields = List.of(
        Optional.ofNullable(stats.getPersonsWhoSharedKeys7daysTrend5percent()),
        Optional.ofNullable(stats.getPersonWhoSharedKeys7daysAvg()),
        Optional.ofNullable(stats.getPersonsWhoSharedKeysCumulated()),
        Optional.ofNullable(stats.getPersonsWhoSharedKeysDaily())
    );

    if (requiredFields.contains(Optional.empty())
        || stats.getPersonWhoSharedKeys7daysAvg() <= 0
        || stats.getPersonsWhoSharedKeysCumulated() <= 0
        || stats.getPersonsWhoSharedKeysDaily() <= 0) {
      return List.of(Optional.empty());
    }

    return requiredFields;
  }


}
