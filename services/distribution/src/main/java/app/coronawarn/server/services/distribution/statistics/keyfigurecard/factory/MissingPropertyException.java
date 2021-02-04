package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.toCardName;

public class MissingPropertyException extends RuntimeException {

  public MissingPropertyException(Integer cardId) {
    super(String.format("Some required properties are missing in JSON file for card %s", toCardName(cardId)));
  }
}
