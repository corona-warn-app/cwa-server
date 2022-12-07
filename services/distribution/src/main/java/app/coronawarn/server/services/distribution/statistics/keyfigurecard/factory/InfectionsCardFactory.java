package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import static app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank.PRIMARY;
import static app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank.SECONDARY;
import static app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank.TERTIARY;
import static app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend.UNSPECIFIED_TREND;
import static app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic.UNSPECIFIED_TREND_SEMANTIC;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.INFECTIONS_CARD;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.ValueTrendCalculator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfectionsCardFactory extends HeaderCardFactory {

  public InfectionsCardFactory(final DistributionServiceConfig config) {
    super(config);
  }

  private static final Logger logger = LoggerFactory.getLogger(InfectionsCardFactory.class);

  @Override
  protected KeyFigureCard buildKeyFigureCard(final StatisticsJsonStringObject stats, final Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(List.of(
        getInfectionsReported(stats),
        getInfectionsAverage(stats),
        getReportsCumulated(stats))).build();
  }

  @Override
  protected int getCardId() {
    return INFECTIONS_CARD.ordinal();
  }

  private KeyFigure getInfectionsAverage(final StatisticsJsonStringObject stats) {
    final var trend = ValueTrendCalculator.from(stats.getInfectionsReported7daysTrend5percent());
    final var semantic = ValueTrendCalculator.getNegativeTrendGrowth(trend);
    return KeyFigure.newBuilder()
        .setValue(stats.getInfectionsReported7daysAvg())
        .setRank(SECONDARY)
        .setDecimals(0)
        .setTrend(trend)
        .setTrendSemantic(semantic)
        .build();
  }

  private KeyFigure getInfectionsReported(final StatisticsJsonStringObject stats) {
    return KeyFigure.newBuilder()
        .setValue(stats.getInfectionsReportedDaily())
        .setRank(PRIMARY)
        .setDecimals(0)
        .setTrend(UNSPECIFIED_TREND)
        .setTrendSemantic(UNSPECIFIED_TREND_SEMANTIC)
        .build();
  }

  private KeyFigure getReportsCumulated(final StatisticsJsonStringObject stats) {
    return KeyFigure.newBuilder()
        .setValue(stats.getInfectionsReportedCumulated())
        .setRank(TERTIARY)
        .setDecimals(0)
        .setTrend(UNSPECIFIED_TREND)
        .setTrendSemantic(UNSPECIFIED_TREND_SEMANTIC)
        .build();
  }

  @Override
  protected List<Optional<Object>> getRequiredFieldValues(final StatisticsJsonStringObject stats) {
    final List<Optional<Object>> requiredFields = List.of(
        Optional.ofNullable(stats.getInfectionsReportedCumulated()),
        Optional.ofNullable(stats.getInfectionsReported7daysTrend5percent()),
        Optional.ofNullable(stats.getInfectionsReported7daysAvg()),
        Optional.ofNullable(stats.getInfectionsReportedDaily()));

    if (requiredFields.contains(Optional.empty())
        || stats.getInfectionsReportedCumulated() <= 0
        || stats.getInfectionsReported7daysAvg() <= 0
        || stats.getInfectionsReportedDaily() <= 0) {
      return List.of(Optional.empty());
    }

    if (stats.getInfectionsReportedDaily() < stats.getInfectionsReported7daysAvg() * thresholdPercent()) {
      logger.warn(
          "skipping '{}' for '{}', because the reported infections ({}) are less than "
              + "{}% of the last 7 days average ({})",
          stats.getEffectiveDate(), INFECTIONS_CARD, stats.getInfectionsReportedDaily(), config.getInfectionThreshold(),
          stats.getInfectionsReported7daysAvg());
      return List.of(Optional.empty());
    }

    return requiredFields;
  }

  double thresholdPercent() {
    return config.getInfectionThreshold() / 100.0;
  }
}
