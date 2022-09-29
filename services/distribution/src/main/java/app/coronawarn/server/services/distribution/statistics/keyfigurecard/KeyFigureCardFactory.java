package app.coronawarn.server.services.distribution.statistics.keyfigurecard;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.getFactoryFor;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KeyFigureCardFactory {

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  public KeyFigureCard createKeyFigureCard(StatisticsJsonStringObject stats, int cardId) {
    return getFactoryFor(cardId, this.distributionServiceConfig).makeKeyFigureCard(stats);
  }
}
