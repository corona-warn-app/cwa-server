package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards;

public class LinkCardFactory extends HeaderCardFactory {

  public LinkCardFactory(final DistributionServiceConfig config, final Cards card) {
    super(config);
    this.card = card;
  }

  private final Cards card;

  @Override
  public int getCardId() {
    return card.ordinal();
  }
}
