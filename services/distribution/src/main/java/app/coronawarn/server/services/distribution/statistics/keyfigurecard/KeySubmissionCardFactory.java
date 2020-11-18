package app.coronawarn.server.services.distribution.statistics.keyfigurecard;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import java.util.List;

public class KeySubmissionCardFactory extends HeaderCardFactory {

  @Override
  protected Integer getCardId() {
    return 3;
  }

  @Override
  protected KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats, Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(List.of(KeyFigure.newBuilder().build(),
        KeyFigure.newBuilder().build(), KeyFigure.newBuilder().build())).build();
  }
}
