package app.coronawarn.server.services.distribution.dcc;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import app.coronawarn.server.services.distribution.dcc.decode.DccRevocationListDecodeException;
import app.coronawarn.server.services.distribution.dcc.decode.DccRevocationListDecoder;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Profile("!fake-dcc-revocation & revocation")
public class ProdDccRevocationClient implements DccRevocationClient {

  private static final Logger logger = LoggerFactory.getLogger(ProdDccRevocationClient.class);

  private final DccRevocationFeignClient dccRevocationFeignClient;
  private final DccRevocationListDecoder dccRevocationListDecoder;
  private String etag = null;

  public ProdDccRevocationClient(DccRevocationFeignClient dccRevocationFeignClient,
      DccRevocationListDecoder dccRevocationListDecoder) {
    this.dccRevocationFeignClient = dccRevocationFeignClient;
    this.dccRevocationListDecoder = dccRevocationListDecoder;
  }

  @Override
  public Optional<List<RevocationEntry>> getDccRevocationList() throws FetchDccListException {
    logger.debug("Get Revocation List from DCC");
    try {
      final ResponseEntity<byte[]> response = dccRevocationFeignClient.getRevocationList();
      final Optional<List<RevocationEntry>> list = Optional.of(dccRevocationListDecoder.decode(response.getBody()));
      etag = getETag(response);
      return list;
    } catch (final DccRevocationListDecodeException e) {
      logger.error("DCC Revocation List could not be decoded.", e);
    } catch (Exception e) {
      throw new FetchDccListException("DCC Revocation List could not be fetched because of: ", e);
    }
    return Optional.empty();
  }

  @Override
  public String getETag() throws FetchDccListException {
    if (etag != null) {
      return etag;
    }
    try {
      etag = getETag(dccRevocationFeignClient.head());
      return etag;
    } catch (final Exception e) {
      throw new FetchDccListException("http-HEAD for DCC Revocation List failed", e);
    }
  }

  /**
   * Get ETag.
   * 
   * @param response from which the ETag should be taken
   * @return ETag.
   * @throws NullPointerException if there is no ETag in the {@link ResponseEntity#getHeaders()}.
   */
  public static String getETag(final ResponseEntity<?> response) {
    final String string = response.getHeaders().getETag().replaceAll("\"", "");
    logger.info("got DCC Revocation List ETag: {}", string);
    return string;
  }
}
