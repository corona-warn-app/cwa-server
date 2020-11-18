package app.coronawarn.server.services.distribution.statistics.keyfigurecard;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;

public class EmptyCardFactory extends HeaderCardFactory {

  @Override
  protected Integer getCardId() {
    return 3;
  }

  @Override
  protected KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats, Builder keyFigureBuilder) {
    return keyFigureBuilder.build();
  }
}
