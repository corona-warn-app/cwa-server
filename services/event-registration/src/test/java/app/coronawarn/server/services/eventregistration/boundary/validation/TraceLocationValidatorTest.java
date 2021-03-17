package app.coronawarn.server.services.eventregistration.boundary.validation;

import app.coronawarn.server.common.protocols.internal.evreg.TraceLocation;
import app.coronawarn.server.services.eventregistration.config.EventRegistrationConfiguration;
import app.coronawarn.server.services.eventregistration.testdata.TestData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import javax.validation.ConstraintValidatorContext;
import java.nio.charset.Charset;
import java.util.Random;

import static app.coronawarn.server.services.eventregistration.testdata.TestData.traceLocation;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TraceLocationValidatorTest {

  @Autowired
  private TraceLocationValidator underTest;

  @Autowired
  private EventRegistrationConfiguration eventRegistrationConfiguration;

  @MockBean
  private ConstraintValidatorContext context;

  @MockBean
  private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

  @BeforeEach
  public void setup() {
    context = mock(ConstraintValidatorContext.class);
    violationBuilder = mock(
        ConstraintValidatorContext.ConstraintViolationBuilder.class);
    when(context.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);
  }

  @Test
  void isValidShouldBeSuccessful() {
    TraceLocation payload = traceLocation()
        .withDescription("description")
        .withStartTimestamp(10)
        .withEndTimestamp(100)
        .withEmptyGuid()
        .withAddress("address")
        .withVersion(5)
        .withDefaultCheckInLength(5).build();

    Assertions.assertThat(underTest.isValid(payload, context)).isTrue();
  }

  @Test
  void isValidShouldFailCheckInLengthMustBePositive() {
    TraceLocation payload = traceLocation()
        .withDescription("description")
        .withStartTimestamp(10)
        .withEndTimestamp(100)
        .withEmptyGuid()
        .withAddress("address")
        .withVersion(5)
        .withDefaultCheckInLength(-5).build();

    Assertions.assertThat(underTest.isValid(payload, context)).isFalse();
  }

  @Test
  void isValidShouldFailEndTimestampBeforeStartTimestamp() {
    TraceLocation payload = traceLocation()
        .withDescription("description")
        .withStartTimestamp(101)
        .withEndTimestamp(100)
        .withEmptyGuid()
        .withAddress("address")
        .withVersion(5)
        .withDefaultCheckInLength(5).build();

    Assertions.assertThat(underTest.isValid(payload, context)).isFalse();
  }

  @Test
  void isValidShouldFailStartOrEndTimestampNegative() {
    TraceLocation payload = traceLocation()
        .withDescription("description")
        .withStartTimestamp(-5)
        .withEndTimestamp(100)
        .withEmptyGuid()
        .withAddress("address")
        .withVersion(5)
        .withDefaultCheckInLength(5).build();

    Assertions.assertThat(underTest.isValid(payload, context)).isFalse();
  }


  @Test
  void isValidShouldFailFieldsAreEmpty() {
    TraceLocation payload = traceLocation()
        .withDescription("")
        .withStartTimestamp(50)
        .withEndTimestamp(100)
        .withEmptyGuid()
        .withAddress("")
        .withVersion(5)
        .withDefaultCheckInLength(5).build();

    Assertions.assertThat(underTest.isValid(payload, context)).isFalse();
  }

  @Test
  void isValidShouldFailDescriptionLongerThan100Characters() {
    byte[] b = new byte[200];
    new Random().nextBytes(b);
    TraceLocation payload = traceLocation()
        .withDescription(new String(b, Charset.defaultCharset()))
        .withStartTimestamp(50)
        .withEndTimestamp(100)
        .withEmptyGuid()
        .withAddress("address")
        .withVersion(5)
        .withDefaultCheckInLength(5).build();

    Assertions.assertThat(underTest.isValid(payload, context)).isFalse();
  }


  @Test
  void isValidShouldFailAddressLongerThan100Characters() {
    byte[] b = new byte[200];
    new Random().nextBytes(b);
    TraceLocation payload = traceLocation()
        .withDescription("description")
        .withStartTimestamp(50)
        .withEndTimestamp(100)
        .withEmptyGuid()
        .withAddress(new String(b, Charset.defaultCharset()))
        .withVersion(5)
        .withDefaultCheckInLength(5).build();

    Assertions.assertThat(underTest.isValid(payload, context)).isFalse();
  }

  @Test
  void isValidShouldFailIncorrectVersion() {
    TraceLocation payload = traceLocation()
        .withDescription("description")
        .withStartTimestamp(50)
        .withEndTimestamp(100)
        .withEmptyGuid()
        .withAddress("address")
        .withVersion(500)
        .withDefaultCheckInLength(5).build();

    Assertions.assertThat(underTest.isValid(payload, context)).isFalse();
  }

  @Test
  void isValidShouldFailGuidIsNotEmpty() {
    TraceLocation payload = traceLocation()
        .withDescription("description")
        .withStartTimestamp(50)
        .withEndTimestamp(100)
        .withGuid(TestData.buildUuid())
        .withAddress("address")
        .withVersion(5)
        .withDefaultCheckInLength(5).build();

    Assertions.assertThat(underTest.isValid(payload, context)).isFalse();
  }
}
