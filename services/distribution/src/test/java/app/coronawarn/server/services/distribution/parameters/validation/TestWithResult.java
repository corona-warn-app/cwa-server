package app.coronawarn.server.services.distribution.parameters.validation;

public class TestWithResult {

  public final String file;
  public final ValidationResult result;

  private TestWithResult(String file, ValidationResult result) {
    this.file = file;
    this.result = result;
  }

  public String path() {
    return "parameters/" + file;
  }

  public static TestWithResult WeightNegative() {
    var expected = new ValidationResult();
    expected.add(new WeightValidationError("transmission", -10d));

    return new TestWithResult("weight_negative.yaml", expected);
  }

  public static TestWithResult WeightTooHigh() {
    var expected = new ValidationResult();
    expected.add(new WeightValidationError("duration", 500d));

    return new TestWithResult("weight_too_high.yaml", expected);
  }

  public static TestWithResult ScoreNegative() {
    var expected = new ValidationResult();
    expected.add(new RiskLevelValidationError("transmission", "appDefined1"));

    return new TestWithResult("score_negative.yaml", expected);
  }

  public static TestWithResult ScoreTooHigh() {
    var expected = new ValidationResult();
    expected.add(new RiskLevelValidationError("transmission", "appDefined3"));

    return new TestWithResult("score_too_high.yaml", expected);
  }

  @Override
  public String toString() {
    return file;
  }
}