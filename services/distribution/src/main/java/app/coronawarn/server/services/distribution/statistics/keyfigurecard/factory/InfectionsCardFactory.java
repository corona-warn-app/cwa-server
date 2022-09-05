package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.INFECTIONS_CARD;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.ValueTrendCalculator;
import app.coronawarn.server.services.distribution.statistics.validation.StatisticsJsonValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

public class InfectionsCardFactory extends HeaderCardFactory {

  private static final Logger logger = LoggerFactory.getLogger(StatisticsJsonValidator.class);

  @Override
  protected int getCardId() {
    return INFECTIONS_CARD.ordinal();
  }

  private KeyFigure getInfectionsReported(StatisticsJsonStringObject stats) {
    return KeyFigure.newBuilder()
        .setValue(stats.getInfectionsReportedDaily())
        .setRank(Rank.PRIMARY)
        .setDecimals(0)
        .setTrend(Trend.UNSPECIFIED_TREND)
        .setTrendSemantic(TrendSemantic.UNSPECIFIED_TREND_SEMANTIC)
        .build();
  }

  private KeyFigure getInfectionsAverage(StatisticsJsonStringObject stats) {
    var trend = ValueTrendCalculator.from(stats.getInfectionsReported7daysTrend5percent());
    var semantic = ValueTrendCalculator.getNegativeTrendGrowth(trend);
    return KeyFigure.newBuilder()
        .setValue(stats.getInfectionsReported7daysAvg())
        .setRank(Rank.SECONDARY)
        .setDecimals(0)
        .setTrend(trend)
        .setTrendSemantic(semantic)
        .build();
  }

  private KeyFigure getReportsCumulated(StatisticsJsonStringObject stats) {
    return KeyFigure.newBuilder()
        .setValue(stats.getInfectionsReportedCumulated())
        .setRank(Rank.TERTIARY)
        .setDecimals(0)
        .setTrend(Trend.UNSPECIFIED_TREND)
        .setTrendSemantic(TrendSemantic.UNSPECIFIED_TREND_SEMANTIC)
        .build();
  }

  @Override
  protected KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats, Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(List.of(
        getInfectionsReported(stats),
        getInfectionsAverage(stats),
        getReportsCumulated(stats))).build();
  }

  @Override
  protected List<Optional<Object>> getRequiredFieldValues(StatisticsJsonStringObject stats) {

    List<Optional<Object>> requiredFields = List.of(
        Optional.ofNullable(stats.getInfectionsReportedCumulated()),
        Optional.ofNullable(stats.getInfectionsReported7daysTrend5percent()),
        Optional.ofNullable(stats.getInfectionsReported7daysAvg()),
        Optional.ofNullable(stats.getInfectionsReportedDaily())
    );

    if (requiredFields.contains(Optional.empty())
        || stats.getInfectionsReportedCumulated() <= 0
        || stats.getInfectionsReported7daysAvg() <= 0
        || stats.getInfectionsReportedDaily() <= 0) {
      return List.of(Optional.empty());
    }

    if (stats.getInfectionsReportedDaily() < stats.getInfectionsReported7daysAvg() * 0.01) {
      logger.warn("skipping '{}' for '{}', because the reported infections is less than {}% of the last 7 days average", stats.getEffectiveDate(),
          INFECTIONS_CARD, 0.01);
      return List.of(Optional.empty());
    }
  //todo add if statement here: log warning (for the infection card, the value is beyond the threshold) + return optional empty
    return requiredFields;
  }
}
