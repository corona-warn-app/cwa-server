
package app.coronawarn.server.services.distribution.dgc.dsc;

import static app.coronawarn.server.common.shared.util.SerializationUtils.readConfiguredJsonOrDefault;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.json.JSONObject;
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
  public List<JSONObject> getDscTrustList() {
    Optional<JSONObject[]> dscTrustList = readConfiguredJsonOrDefault(resourceLoader, null,
        "trustList/ubirchDSC.json", JSONObject[].class);

    if (dscTrustList.isEmpty()) {
      return Collections.emptyList();
    }
    return Arrays.asList(dscTrustList.get());
  }
}
