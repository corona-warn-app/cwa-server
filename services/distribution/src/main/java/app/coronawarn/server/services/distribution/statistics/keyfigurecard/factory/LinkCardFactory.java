package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.PANDEMIC_RADAR_CARD;

public class LinkCardFactory extends HeaderCardFactory {

  @Override
  public int getCardId() {
    return PANDEMIC_RADAR_CARD.ordinal();
  }
}
