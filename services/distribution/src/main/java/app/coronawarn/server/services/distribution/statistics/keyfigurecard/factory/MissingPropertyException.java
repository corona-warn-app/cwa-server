package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import java.util.List;

public class MissingPropertyException extends RuntimeException {

  public MissingPropertyException(Integer cardId) {
    super(String.format("Some required properties are missing in JSON file for card with ID %s", cardId));
  }
}
