package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import java.util.List;

public class MissingPropertyException extends RuntimeException {

  public MissingPropertyException(List<String> properties) {
    super(String.format("Properties [%s] missing in JSON file", String.join(", ", properties)));
  }
}
