package app.coronawarn.server.services.distribution.dgc.dsc;

import app.coronawarn.server.common.persistence.domain.DccRevocationEntry;
import app.coronawarn.server.services.distribution.dcc.FetchDccListException;
import app.coronawarn.server.services.distribution.dcc.decode.DccRevocationListDecoder;
import com.google.protobuf.ByteString;
import java.io.InputStream;
import java.util.List;
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
  private DccRevocationListDecoder dccRevocationListDecoder;

  public TestDigitalSigningCertificateClient(ResourceLoader resourceLoader,
      DccRevocationListDecoder dccRevocationListDecoder) {
    this.resourceLoader = resourceLoader;
    this.dccRevocationListDecoder = dccRevocationListDecoder;
  }

  @Override
  public Optional<List<DccRevocationEntry>> getDscTrustList() throws FetchDccListException {
    InputStream input = getClass().getResourceAsStream("/revocation/chunk.lst");
    try {
      return Optional.of(dccRevocationListDecoder.decode(ByteString.copyFrom(input.readAllBytes())));
    } catch (Exception e) {
      throw new FetchDccListException("DCC Revocation List could not be fetched because of: ", e);
    }
  }
}
