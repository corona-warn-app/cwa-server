package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.EMPTY_CARD;

public class EmptyCardFactory extends HeaderCardFactory {

  @Override
  protected int getCardId() {
    return EMPTY_CARD.ordinal();
  }
}
