package app.coronawarn.server.services.distribution.dcc.decode;

import static app.coronawarn.server.common.shared.util.SerializationUtils.jsonExtractCosePayload;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.exception.DscListDecodeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!fake-dcc-revocation")
public class DccRevocationListDecoder {

  private static final Logger logger = LoggerFactory.getLogger(DccRevocationListDecoder.class);

  public static final char CONTENT_STARTS_CHAR = '{';
  private final DistributionServiceConfig distributionServiceConfig;

  public DccRevocationListDecoder(DistributionServiceConfig distributionServiceConfig) {
    this.distributionServiceConfig = distributionServiceConfig;
  }

  /**
   * Decode the trust list of certificates. Verifies the trust list content by using the ECDSA signature logic. Filters
   * only X509 valid format certificates from the response.
   *
   * @param data - trust list response from DSC as string.
   * @return - object wrapping the list of certificates.
   * @throws DscListDecodeException - thrown if any exception is caught and special treatment if signature verification
   *                                fails.
   */
  public List<RevocationEntry> decode(byte[] data) throws DccRevocationListDecodeException {
    ArrayList<RevocationEntry> revocationEntries = new ArrayList<>();
    try {
      Map<byte[], List<byte[]>> jsonPayload = jsonExtractCosePayload(data);

      jsonPayload.forEach((keyAndType, values) -> {
        byte[] kid = Arrays.copyOfRange(keyAndType, 0, keyAndType.length - 1);
        byte[] type = Arrays.copyOfRange(keyAndType, keyAndType.length - 1, keyAndType.length);

        values.forEach(hash -> {
          revocationEntries.add(new RevocationEntry(kid, type, hash,
              Arrays.copyOfRange(hash, 0, 1),
              Arrays.copyOfRange(hash, 1, 2)));
        });
      });
    } catch (Exception e) {
      throw new DccRevocationListDecodeException("DCC revocation list NOT decoded.", e);
    }
    return revocationEntries;
  }
}
