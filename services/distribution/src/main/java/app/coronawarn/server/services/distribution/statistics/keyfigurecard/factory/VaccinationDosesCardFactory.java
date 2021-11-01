package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.VACCINATION_DOSES_CARD;

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

public class VaccinationDosesCardFactory extends HeaderCardFactory {

  @Override
  protected int getCardId() {
    return VACCINATION_DOSES_CARD.ordinal();
  }

  private KeyFigure getAdministeredDosesDaily(StatisticsJsonStringObject stats) {
    return KeyFigure.newBuilder()
        .setValue(stats.getAdministeredDosesDaily())
        .setRank(Rank.PRIMARY)
        .setDecimals(0)
        .setTrend(Trend.UNSPECIFIED_TREND)
        .setTrendSemantic(TrendSemantic.UNSPECIFIED_TREND_SEMANTIC)
        .build();
  }

  private KeyFigure getAdministeredDoses7daysAverage(StatisticsJsonStringObject stats) {
    var trend = ValueTrendCalculator.from(stats.getAdministeredDoses7daysAvgTrend5percent());
    var semantic = ValueTrendCalculator.getPositiveTrendGrowth(trend);

    return KeyFigure.newBuilder()
        .setValue(stats.getAdministeredDoses7daysAvg())
        .setRank(Rank.SECONDARY)
        .setDecimals(0)
        .setTrend(trend)
        .setTrendSemantic(semantic)
        .build();
  }

  private KeyFigure getAdministeredDosesCumulated(StatisticsJsonStringObject stats) {
    return KeyFigure.newBuilder()
        .setValue(stats.getAdministeredDosesCumulated())
        .setRank(Rank.TERTIARY)
        .setDecimals(0)
        .setTrend(Trend.UNSPECIFIED_TREND)
        .setTrendSemantic(TrendSemantic.UNSPECIFIED_TREND_SEMANTIC)
        .build();
  }

  @Override
  protected KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats, Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(List.of(
        getAdministeredDosesDaily(stats),
        getAdministeredDoses7daysAverage(stats),
        getAdministeredDosesCumulated(stats))).build();
  }

  @Override
  protected List<Optional<Object>> getRequiredFieldValues(StatisticsJsonStringObject stats) {
    List<Optional<Object>> requiredFields = List.of(
        Optional.ofNullable(stats.getAdministeredDosesDaily()),
        Optional.ofNullable(stats.getAdministeredDoses7daysAvg()),
        Optional.ofNullable(stats.getAdministeredDosesCumulated()),
        Optional.ofNullable(stats.getAdministeredDoses7daysAvgTrend5percent()));

    if (requiredFields.contains(Optional.empty())
        || stats.getAdministeredDosesDaily() <= 0
        || stats.getAdministeredDoses7daysAvg() <= 0
        || stats.getAdministeredDosesCumulated() <= 0
    ) {
      return List.of(Optional.empty());
    }

    return requiredFields;
  }
}
