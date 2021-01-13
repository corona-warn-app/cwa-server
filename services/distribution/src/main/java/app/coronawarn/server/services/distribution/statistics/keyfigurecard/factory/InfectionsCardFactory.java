package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.ValueTrendCalculator;
import java.util.List;
import java.util.Optional;
import org.springframework.data.util.Pair;

public class InfectionsCardFactory extends HeaderCardFactory {

  public InfectionsCardFactory(ValueTrendCalculator valueTrendCalculator) {
    super(valueTrendCalculator);
  }

  @Override
  protected Integer getCardId() {
    return KeyFigureCardSequenceConstants.INFECTIONS_CARD_ID;
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
  protected List<Pair<String, Optional<Object>>> getNonNullFields(StatisticsJsonStringObject stats) {
    return List.of(
        Pair.of("infections_effective_daily", Optional.ofNullable(stats.getInfectionsReportedDaily())),
        Pair.of("infections_effective_7days_avg", Optional.ofNullable(stats.getInfectionsReported7daysAvg())),
        Pair.of("infections_effective _7days_avg_trend_5percent",
            Optional.ofNullable(stats.getInfectionsReported7daysTrend5percent())),
        Pair.of("infections_effective_cumulated", Optional.ofNullable(stats.getInfectionsReportedCumulated()))
    );
  }
}
