package app.coronawarn.server.services.distribution.dgc.client.signature;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import org.springframework.http.ResponseEntity;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class ValidSignatureValidator implements ConstraintValidator<VerifyDccSignature, ResponseEntity<?>> {

  @Override
  public boolean isValid(ResponseEntity responseEntity, ConstraintValidatorContext constraintValidatorContext) {
    System.out.println("test");
    return false;
  }
}
