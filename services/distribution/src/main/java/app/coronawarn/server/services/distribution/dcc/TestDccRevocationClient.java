package app.coronawarn.server.services.distribution.dcc;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import app.coronawarn.server.services.distribution.dcc.decode.DccRevocationListDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@Profile({"fake-dcc-revocation", "!revocation"})
public class TestDccRevocationClient implements DccRevocationClient {

  private static final Logger logger = LoggerFactory.getLogger(TestDccRevocationClient.class);

  public static final String REVOCATION_CHUNK_LST = "revocation/chunk.lst";
  private final ResourceLoader resourceLoader;
  private final DccRevocationListDecoder dccRevocationListDecoder;

  public TestDccRevocationClient(ResourceLoader resourceLoader, DccRevocationListDecoder dccRevocationListDecoder) {
    this.dccRevocationListDecoder = dccRevocationListDecoder;
    this.resourceLoader = resourceLoader;
  }

  @Override
  public Optional<List<RevocationEntry>> getDccRevocationList() throws FetchDccListException {
    try (InputStream input = resourceLoader.getClassLoader().getResourceAsStream(REVOCATION_CHUNK_LST)) {
      return Optional.of(dccRevocationListDecoder.decode(input.readAllBytes()));
    } catch (IOException e) {
      logger.error("Error reading {} cose object.", REVOCATION_CHUNK_LST, e);
    } catch (Exception e) {
      throw new FetchDccListException("DCC Revocation List could not be fetched because of: ", e);
    }
    return Optional.empty();
  }
}
