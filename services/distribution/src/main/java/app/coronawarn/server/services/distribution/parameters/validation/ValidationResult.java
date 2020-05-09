package app.coronawarn.server.services.distribution.parameters.validation;

import java.util.HashSet;

public class ValidationResult extends HashSet<ValidationError> {

  public boolean hasErrors() {
    return !this.isEmpty();
  }


  public boolean isSuccessful() {
    return !hasErrors();
  }
}
