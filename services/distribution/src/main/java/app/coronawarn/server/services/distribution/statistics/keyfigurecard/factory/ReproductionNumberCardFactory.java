package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.ValueTrendCalculator;
import java.util.List;
import java.util.Optional;
import org.springframework.data.util.Pair;

public class ReproductionNumberCardFactory extends HeaderCardFactory {

  public ReproductionNumberCardFactory(ValueTrendCalculator valueTrendCalculator) {
    super(valueTrendCalculator);
  }

  @Override
  protected Integer getCardId() {
    return KeyFigureCardSequenceConstants.REPRODUCTION_NUMBER_CARD;
  }

  @Override
  protected KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats, Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(List.of(
        getSevenDayRValueKeyFigure(stats))).build();
  }

  private KeyFigure getSevenDayRValueKeyFigure(StatisticsJsonStringObject stats) {
    var trend = ValueTrendCalculator.from(stats.getSevenDayRvalue1stReportedTrend1percent());
    var semantic = ValueTrendCalculator.getNegativeTrendGrowth(trend);
    return KeyFigure.newBuilder()
        .setValue(stats.getSevenDayRvalue1stReportedDaily())
        .setRank(Rank.PRIMARY)
        .setDecimals(2)
        .setTrend(trend)
        .setTrendSemantic(semantic)
        .build();
  }

  @Override
  protected List<Pair<String, Optional<Object>>> getNonNullFields(StatisticsJsonStringObject stats) {
    return List.of(
        Pair.of("seven_day_r_value_1st_reported_trend_1percent", Optional.ofNullable(stats.getSevenDayRvalue1stReportedDaily())),
        Pair.of("seven_day_r_value_1st_reported_daily", Optional.ofNullable(stats.getSevenDayRvalue1stReportedDaily()))
    );
  }
}
