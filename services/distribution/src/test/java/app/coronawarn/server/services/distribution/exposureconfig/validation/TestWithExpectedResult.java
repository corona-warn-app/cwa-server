package app.coronawarn.server.services.distribution.exposureconfig.validation;

public class TestWithExpectedResult {

  public final String file;

  public final ValidationResult result = new ValidationResult();

  public TestWithExpectedResult(String file) {
    this.file = file;
  }

  public TestWithExpectedResult with(ValidationError error) {
    this.result.add(error);
    return this;
  }

  public String path() {
    return "parameters/" + file;
  }

  @Override
  public String toString() {
    return file;
  }
}