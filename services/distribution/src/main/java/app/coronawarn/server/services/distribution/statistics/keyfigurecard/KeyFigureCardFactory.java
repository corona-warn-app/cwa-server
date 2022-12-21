package app.coronawarn.server.services.distribution.statistics.keyfigurecard;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.getFactoryFor;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.values;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.EmptyCardFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KeyFigureCardFactory {

  private static final Logger logger = LoggerFactory.getLogger(KeyFigureCardFactory.class);

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  /**
   * Creates a new statistics card, based upon the given ordinal.
   *
   * @param stats  data for the card.
   * @param cardId ordinal number of {@link Cards}.
   * @return a new {@link KeyFigureCard}.
   */
  public KeyFigureCard createKeyFigureCard(final StatisticsJsonStringObject stats, final int cardId) {
    try {
      return getFactoryFor(values()[cardId], distributionServiceConfig).makeKeyFigureCard(stats);
    } catch (final IndexOutOfBoundsException e) {
      logger.error(Cards.class + " doesn't contain value for: " + cardId, e);
      return new EmptyCardFactory(distributionServiceConfig).makeKeyFigureCard(stats);
    }
  }
}
