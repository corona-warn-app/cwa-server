package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.BOOSTER_VACCINATED_CARD;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import java.util.List;
import java.util.Optional;

public class BoosterVaccinatedCardFactory extends HeaderCardFactory {

  @Override
  protected int getCardId() {
    return BOOSTER_VACCINATED_CARD.ordinal();
  }


  private KeyFigure getPersonsThirdDoseRatio(StatisticsJsonStringObject stats) {
    return KeyFigure.newBuilder()
        .setValue(stats.getPersonsWithThirdDoseRatio())
        .setRank(Rank.PRIMARY)
        .setDecimals(1)
        .setTrend(Trend.UNSPECIFIED_TREND)
        .setTrendSemantic(TrendSemantic.UNSPECIFIED_TREND_SEMANTIC)
        .build();
  }

  private KeyFigure getPersonsThirdDoseCumulated(StatisticsJsonStringObject stats) {
    return KeyFigure.newBuilder()
        .setValue(stats.getPersonsWithThirdDoseCumulated())
        .setRank(Rank.TERTIARY)
        .setDecimals(0)
        .setTrend(Trend.UNSPECIFIED_TREND)
        .setTrendSemantic(TrendSemantic.UNSPECIFIED_TREND_SEMANTIC)
        .build();
  }

  @Override
  protected KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats, Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(List.of(
        getPersonsThirdDoseRatio(stats),
        getPersonsThirdDoseCumulated(stats)
    )).build();
  }

  @Override
  protected List<Optional<Object>> getRequiredFieldValues(StatisticsJsonStringObject stats) {
    List<Optional<Object>> requiredFields = List.of(
        Optional.ofNullable(stats.getPersonsWithThirdDoseRatio()),
        Optional.ofNullable(stats.getPersonsWithThirdDoseCumulated()));

    if (requiredFields.contains(Optional.empty())
        || stats.getPersonsFullyVaccinatedRatio() <= 0
        || stats.getPersonsFullyVaccinatedCumulated() <= 0
    ) {
      return List.of(Optional.empty());
    }

    return requiredFields;
  }
}
