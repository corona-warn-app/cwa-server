package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.Cards.getNameFor;

public class MissingPropertyException extends RuntimeException {

  private static final long serialVersionUID = -135199548086774034L;

  public MissingPropertyException(int cardId) {
    super(String.format("Some required properties are missing in JSON file for card %s", getNameFor(cardId)));
  }
}
