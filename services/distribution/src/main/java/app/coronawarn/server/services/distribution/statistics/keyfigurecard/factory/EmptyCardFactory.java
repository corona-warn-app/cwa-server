package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.EMPTY_CARD;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;

public class EmptyCardFactory extends HeaderCardFactory {

  public EmptyCardFactory(final DistributionServiceConfig config) {
    super(config);
  }

  @Override
  protected int getCardId() {
    return EMPTY_CARD.ordinal();
  }
}
