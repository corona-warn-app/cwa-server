package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.ValueTrendCalculator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.data.util.Pair;

public class EmptyCardFactory extends HeaderCardFactory {

  public EmptyCardFactory(ValueTrendCalculator valueTrendCalculator) {
    super(valueTrendCalculator);
  }

  @Override
  protected Integer getCardId() {
    return KeyFigureCardSequenceConstants.REPRODUCTION_NUMBER_CARD;
  }

  @Override
  protected KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats, Builder keyFigureBuilder) {
    return keyFigureBuilder.build();
  }

  @Override
  protected List<Pair<String, Optional<Object>>> getNonNullFields(StatisticsJsonStringObject stats) {
    return Collections.emptyList();
  }
}
