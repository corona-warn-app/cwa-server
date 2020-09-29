

package app.coronawarn.server.common.persistence.domain.normalization;

/**
 *  Responsible for sanitizing the information within a diagnosis key to fit the national specification.
 *  This can mean, for example, that either this service will initialize missing values that were not provided
 *  during submission and other injection channels, or it can mean restructuring the values that were
 *  provided, which might be valid, but not meeting national standards, as described by the system configuration
 *  (i.e. number scales, ranges etc).
 *
 *  <p>There is a slight overlap with validation topics from a class responsability point of view, but the
 *  the focus of this service is to directly modify information in given keys, rather than stoping a process
 *  when the key does not meet input validty requirements.
 *
 *  <p>There is no default normalization instance provided in the domain module. All other modules which
 *  depend on 'persistence' must provide their own implementation since this operation can be contextual.
 */
@FunctionalInterface
public interface DiagnosisKeyNormalizer {

  /**
   * Given a container of fields from the {@link DiagnosisKey} with their respective values,
   * return a new container with the normalized values.
   */
  NormalizableFields normalize(NormalizableFields fieldsAndValues);
}
