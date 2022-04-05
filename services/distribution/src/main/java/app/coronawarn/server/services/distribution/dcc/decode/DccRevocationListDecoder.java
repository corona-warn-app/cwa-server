package app.coronawarn.server.services.distribution.dcc.decode;

import static app.coronawarn.server.common.shared.util.SecurityUtils.base64decode;
import static app.coronawarn.server.common.shared.util.SerializationUtils.jsonExtractCosePayload;

import app.coronawarn.server.common.persistence.domain.DccRevocationEntry;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.CertificateStructure;
import app.coronawarn.server.services.distribution.dgc.Certificates;
import app.coronawarn.server.services.distribution.dgc.exception.DscListDecodeException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.json.simple.JSONObject;
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
  public List<DccRevocationEntry> decode(ByteString data) throws DccRevocationListDecodeException {
    ArrayList<DccRevocationEntry> revocationEntries = new ArrayList<>();
    try {
      /**TODO: Signature verification
       * PublicKey publicKey =
       * getPublicKeyFromString(distributionServiceConfig.getDccRevocation().getClient().getPublicKey());
       *
       */
      ObjectMapper mapper = new ObjectMapper();
      JSONObject jsonPayload = jsonExtractCosePayload(data.toByteArray());

      jsonPayload.forEach((keyAndType, values) -> {
        byte[] kid = keyAndType.toString().substring(0, keyAndType.toString().length() - 1).getBytes();
        byte[] type = keyAndType.toString().substring(keyAndType.toString().length() - 1).getBytes();
        List<String> valuesArray = new ArrayList<>();
        try {
          valuesArray = mapper.readValue(values.toString(), new TypeReference<>() {
          });
        } catch (IOException e) {
          e.printStackTrace();
        }
        valuesArray.forEach(hash -> {
          revocationEntries.add(new DccRevocationEntry(kid, type, hash.getBytes(),
              hash.substring(0, 2).getBytes(), hash.substring(2, 4).getBytes()));
        });
      });
    } catch (Exception e) {
      throw new DccRevocationListDecodeException("DSC list NOT decoded.", e);
    }
    return revocationEntries  ;
  }
}
