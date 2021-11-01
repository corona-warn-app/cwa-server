package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.FIRST_VACCINATION_CARD;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import java.util.List;
import java.util.Optional;

public class FirstVaccinationCardFactory extends HeaderCardFactory {

  @Override
  protected int getCardId() {
    return FIRST_VACCINATION_CARD.ordinal();
  }

  private KeyFigure getPersonsWithFirstDoseRatio(StatisticsJsonStringObject stats) {
    return KeyFigure.newBuilder()
        .setValue(stats.getPersonsWithFirstDoseRatio())
        .setRank(Rank.PRIMARY)
        .setDecimals(1)
        .setTrend(Trend.UNSPECIFIED_TREND)
        .setTrendSemantic(TrendSemantic.UNSPECIFIED_TREND_SEMANTIC)
        .build();
  }

  private KeyFigure getPersonsWithFirstDoseCumulated(StatisticsJsonStringObject stats) {
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
        getPersonsWithFirstDoseCumulated(stats))).build();
  }

  @Override
  protected List<Optional<Object>> getRequiredFieldValues(StatisticsJsonStringObject stats) {
    List<Optional<Object>> requiredFields = List.of(
        Optional.ofNullable(stats.getPersonsWithFirstDoseRatio()),
        Optional.ofNullable(stats.getPersonsWithFirstDoseCumulated())
    );

    if (requiredFields.contains(Optional.empty())
        || stats.getPersonsWithFirstDoseCumulated() <= 0
        || stats.getPersonsWithFirstDoseRatio() <= 0) {
      return List.of(Optional.empty());
    }
    return requiredFields;
  }
}
