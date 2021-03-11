package app.coronawarn.server.services.eventregistration.boundary.validation;



import app.coronawarn.server.common.protocols.internal.evreg.TraceLocation;
import app.coronawarn.server.services.eventregistration.config.EventRegistrationConfiguration;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class TraceLocationValidator implements ConstraintValidator<ValidTraceLocation, TraceLocation> {

  private Integer version;
  private final EventRegistrationConfiguration eventRegistrationConfiguration;


  public TraceLocationValidator(EventRegistrationConfiguration eventRegistrationConfiguration) {
    this.eventRegistrationConfiguration = eventRegistrationConfiguration;
    this.version = this.eventRegistrationConfiguration.getVersion();
  }

  @Override
  public boolean isValid(TraceLocation value, ConstraintValidatorContext context) {
    return true;
  }
}
