package app.coronawarn.server.services.distribution.dgc.dsc;

import static app.coronawarn.server.common.shared.util.SerializationUtils.readConfiguredJsonOrDefault;

import app.coronawarn.server.services.distribution.dgc.Certificates;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * This is an implementation with test data for interface retrieving Digital Signign Certificates data. Used to retrieve
 * mock sample data from classpath.
 */
@Component
@Profile("fake-dsc-client")
public class TestDigitalSigningCertificateClient implements DigitalSigningCertificatesClient {

  private final ResourceLoader resourceLoader;

  public TestDigitalSigningCertificateClient(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public Optional<Certificates> getDscTrustList() {
    return readConfiguredJsonOrDefault(resourceLoader, null, "trustList/ubirchDSC.json", Certificates.class);
  }
}
