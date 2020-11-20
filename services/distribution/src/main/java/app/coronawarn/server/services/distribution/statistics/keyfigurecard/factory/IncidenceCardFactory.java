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

public class IncidenceCardFactory extends HeaderCardFactory {

  public IncidenceCardFactory(ValueTrendCalculator valueTrendCalculator) {
    super(valueTrendCalculator);
  }

  @Override
  protected Integer getCardId() {
    return KeyFigureCardSequenceConstants.INCIDENCE_CARD_ID;
  }

  private KeyFigure getIncidence(StatisticsJsonStringObject stats) {
    var trend = valueTrendCalculator.getTrend(stats.getSevenDayIncidenceGrowthrate());
    var semantic = valueTrendCalculator.getNegativeTrendGrowth(trend);
    return KeyFigure.newBuilder()
        .setValue(stats.getSevenDayIncidence())
        .setRank(Rank.PRIMARY)
        .setTrend(trend)
        .setTrendSemantic(semantic)
        .setDecimals(1)
        .build();
  }

  @Override
  protected KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats, Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(
        List.of(this.getIncidence(stats)))
        .build();
  }

  @Override
  protected List<Pair<String, Optional<Object>>> getNonNullFields(StatisticsJsonStringObject stats) {
    return List.of(
        Pair.of("seven_day_incidence", Optional.ofNullable(stats.getSevenDayIncidence())),
        Pair.of("seven_day_incidence_growthrate", Optional.ofNullable(stats.getSevenDayIncidenceGrowthrate()))
    );
  }
}
