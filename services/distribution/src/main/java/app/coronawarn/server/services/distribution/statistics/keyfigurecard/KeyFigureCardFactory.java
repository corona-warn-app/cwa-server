package app.coronawarn.server.services.distribution.statistics.keyfigurecard;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.getFactoryFor;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import org.springframework.stereotype.Component;

@Component
public class KeyFigureCardFactory {

  public KeyFigureCard createKeyFigureCard(StatisticsJsonStringObject stats, int cardId) {
    return getFactoryFor(cardId).makeKeyFigureCard(stats);
  }
}
