package app.coronawarn.server.services.distribution.dcc;

import app.coronawarn.server.common.persistence.domain.DccRevocationEntry;
import app.coronawarn.server.services.distribution.dcc.decode.DccRevocationListDecoder;
import com.google.protobuf.ByteString;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@Profile("fake-dcc-revocation")
public class TestDccRevocationClient implements DccRevocationClient {

  private final ResourceLoader resourceLoader;
  private final DccRevocationListDecoder dccRevocationListDecoder;

  public TestDccRevocationClient(ResourceLoader resourceLoader, DccRevocationListDecoder dccRevocationListDecoder) {
    this.dccRevocationListDecoder = dccRevocationListDecoder;
    this.resourceLoader = resourceLoader;
  }

  @Override
  public Optional<List<DccRevocationEntry>> getDccRevocationList() throws FetchDccListException {
    InputStream input = resourceLoader.getClassLoader().getResourceAsStream("/revocation/chunk.lst");
    try {
      return Optional.of(dccRevocationListDecoder.decode(ByteString.copyFrom(input.readAllBytes())));
    } catch (Exception e) {
      throw new FetchDccListException("DCC Revocation List could not be fetched because of: ", e);
    }
  }
}
