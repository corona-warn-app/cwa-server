package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.FULLY_VACCINATED_CARD;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import java.util.List;
import java.util.Optional;

public class FullyVaccinatedCardFactory extends HeaderCardFactory {

  @Override
  protected int getCardId() {
    return FULLY_VACCINATED_CARD.ordinal();
  }

  private KeyFigure getPersonsFullyVaccinatedRatio(StatisticsJsonStringObject stats) {
    return KeyFigure.newBuilder()
        .setValue(stats.getPersonsFullyVaccinatedRatio())
        .setRank(Rank.PRIMARY)
        .setDecimals(1)
        .setTrend(Trend.UNSPECIFIED_TREND)
        .setTrendSemantic(TrendSemantic.UNSPECIFIED_TREND_SEMANTIC)
        .build();
  }

  private KeyFigure getPersonsFullyVaccinatedCumulated(StatisticsJsonStringObject stats) {
    return KeyFigure.newBuilder()
        .setValue(stats.getPersonsFullyVaccinatedCumulated())
        .setRank(Rank.TERTIARY)
        .setDecimals(0)
        .setTrend(Trend.UNSPECIFIED_TREND)
        .setTrendSemantic(TrendSemantic.UNSPECIFIED_TREND_SEMANTIC)
        .build();
  }

  @Override
  protected KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats, Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(List.of(
        getPersonsFullyVaccinatedRatio(stats),
        getPersonsFullyVaccinatedCumulated(stats)
    )).build();
  }

  @Override
  protected List<Optional<Object>> getRequiredFieldValues(StatisticsJsonStringObject stats) {
    List<Optional<Object>> requiredFields = List.of(
        Optional.ofNullable(stats.getPersonsFullyVaccinatedRatio()),
        Optional.ofNullable(stats.getPersonsFullyVaccinatedCumulated()));

    if (requiredFields.contains(Optional.empty())
        || stats.getPersonsFullyVaccinatedRatio() <= 0
        || stats.getPersonsFullyVaccinatedCumulated() <= 0
    ) {
      return List.of(Optional.empty());
    }

    return requiredFields;
  }
}
