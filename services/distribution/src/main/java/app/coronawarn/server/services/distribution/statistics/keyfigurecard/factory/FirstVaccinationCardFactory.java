package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants;
import java.util.List;
import java.util.Optional;

public class FirstVaccinationCardFactory extends HeaderCardFactory {

  @Override
  protected Integer getCardId() {
    return KeyFigureCardSequenceConstants.FIRST_VACCINATION_CARD;  }

  private KeyFigure getPersonsWithFirstDoseRatio(StatisticsJsonStringObject stats) {
    return KeyFigure.newBuilder()
        .setValue(stats.getPersonsWithFirstDoseRatio())
        .setRank(Rank.PRIMARY)
        .setDecimals(1)
        .setTrend(Trend.UNSPECIFIED_TREND)
        .setTrendSemantic(TrendSemantic.UNSPECIFIED_TREND_SEMANTIC)
        .build();
  }

  private KeyFigure getPersonsWithFirstDoseCumulate(StatisticsJsonStringObject stats) {
    return KeyFigure.newBuilder()
        .setValue(stats.getPersonsWithFirstDoseCumulated())
        .setRank(Rank.TERTIARY)
        .setDecimals(0)
        .setTrend(Trend.UNSPECIFIED_TREND)
        .setTrendSemantic(TrendSemantic.UNSPECIFIED_TREND_SEMANTIC)
        .build();
  }

  @Override
  protected KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats, Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(List.of(
        getPersonsWithFirstDoseRatio(stats),
        getPersonsWithFirstDoseCumulate(stats))).build();
  }

  @Override
  protected List<Optional<Object>> getRequiredFieldValues(StatisticsJsonStringObject stats) {
    return null;
  }
}
