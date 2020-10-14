

package app.coronawarn.server.services.federation.upload.payload.signing;

import static app.coronawarn.server.services.federation.upload.UploadLogMessages.FAILED_BYTE_ARRAY_TO_STRING_CONVERSION;

import app.coronawarn.server.common.Logger;
import app.coronawarn.server.common.LoggerFactory;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import com.google.protobuf.ByteString;
import com.google.protobuf.ProtocolStringList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.stereotype.Component;

@Component
public class BatchSigner {

  private static final Logger logger = LoggerFactory.getLogger(BatchSigner.class);

  private final CryptoProvider cryptoProvider;

  private final UploadServiceConfig uploadServiceConfig;

  public BatchSigner(CryptoProvider cryptoProvider,
      UploadServiceConfig uploadServiceConfig) {
    this.cryptoProvider = cryptoProvider;
    this.uploadServiceConfig = uploadServiceConfig;
  }

  private String bytesToBase64String(byte[] bytes) {
    try {
      return Base64.getEncoder().encodeToString(bytes);
    } catch (IllegalArgumentException e) {
      logger.error(FAILED_BYTE_ARRAY_TO_STRING_CONVERSION);
      return null;
    }
  }

  private byte[] createBytesToSign(final DiagnosisKeyBatch batch) {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    sortBatchByKeyData(batch).stream()
        .map(this::createBytesForKey)
        .sorted(Comparator.nullsLast(
            Comparator.comparing(this::bytesToBase64String)
        ))
        .forEach(buffer::writeBytes);
    return buffer.toByteArray();
  }

  private byte[] createBytesForKey(final DiagnosisKey diagnosisKey) {
    final ByteArrayOutputStream batchBytes = new ByteArrayOutputStream();
    writeBytesInByteArray(diagnosisKey.getKeyData(), batchBytes);
    writeSeparatorInArray(batchBytes);
    writeIntInByteArray(diagnosisKey.getRollingStartIntervalNumber(), batchBytes);
    writeSeparatorInArray(batchBytes);
    writeIntInByteArray(diagnosisKey.getRollingPeriod(), batchBytes);
    writeSeparatorInArray(batchBytes);
    writeIntInByteArray(diagnosisKey.getTransmissionRiskLevel(), batchBytes);
    writeSeparatorInArray(batchBytes);
    writeVisitedCountriesInByteArray(diagnosisKey.getVisitedCountriesList(),
        batchBytes);
    writeSeparatorInArray(batchBytes);
    writeB64StringInByteArray(diagnosisKey.getOrigin(), batchBytes);
    writeSeparatorInArray(batchBytes);
    writeIntInByteArray(diagnosisKey.getReportType().getNumber(), batchBytes);
    writeSeparatorInArray(batchBytes);
    writeIntInByteArray(diagnosisKey.getDaysSinceOnsetOfSymptoms(), batchBytes);
    writeSeparatorInArray(batchBytes);
    return batchBytes.toByteArray();
  }

  private void writeBytesInByteArray(final ByteString bytes, ByteArrayOutputStream byteArray) {
    String base64String = bytesToBase64String(bytes.toByteArray());
    if (base64String != null) {
      writeStringInByteArray(base64String, byteArray);
    }
  }

  private void writeStringInByteArray(final String batchString, final ByteArrayOutputStream byteArray) {
    byteArray.writeBytes(batchString.getBytes(StandardCharsets.US_ASCII));
  }

  private void writeB64StringInByteArray(final String batchString, final ByteArrayOutputStream byteArray) {
    String base64String = bytesToBase64String(batchString.getBytes(StandardCharsets.US_ASCII));
    if (base64String != null) {
      writeStringInByteArray(base64String, byteArray);
    }
  }

  private void writeIntInByteArray(final int batchInt, final ByteArrayOutputStream byteArray) {
    String base64String = bytesToBase64String(ByteBuffer.allocate(4).putInt(batchInt).array());
    if (base64String != null) {
      writeStringInByteArray(base64String, byteArray);
    }
  }

  private void writeSeparatorInArray(final ByteArrayOutputStream byteArray) {
    byteArray.writeBytes(".".getBytes(StandardCharsets.US_ASCII));
  }

  private void writeVisitedCountriesInByteArray(final ProtocolStringList countries,
      final ByteArrayOutputStream byteArray) {
    writeB64StringInByteArray(String.join(",", countries), byteArray);
  }


  private static List<DiagnosisKey> sortBatchByKeyData(DiagnosisKeyBatch batch) {
    return batch.getKeysList()
        .stream()
        .sorted(Comparator.comparing(diagnosisKey -> diagnosisKey.getKeyData().toStringUtf8()))
        .collect(Collectors.toList());
  }

  private SignerInfoGenerator createSignerInfo(X509Certificate cert)
      throws OperatorCreationException, CertificateEncodingException {
    return new JcaSignerInfoGeneratorBuilder(createDigestBuilder()).build(createContentSigner(), cert);
  }

  private X509CertificateHolder createCertificateHolder(X509Certificate cert)
      throws CertificateEncodingException, IOException {
    return new X509CertificateHolder(cert.getEncoded());
  }

  private DigestCalculatorProvider createDigestBuilder() throws OperatorCreationException {
    return new JcaDigestCalculatorProviderBuilder().build();

  }

  private ContentSigner createContentSigner() throws OperatorCreationException {
    return new JcaContentSignerBuilder(uploadServiceConfig.getSignature().getAlgorithmName())
        .build(cryptoProvider.getPrivateKey());
  }

  private String sign(final byte[] data, X509Certificate cert)
      throws CertificateEncodingException, OperatorCreationException, IOException, CMSException {
    final CMSSignedDataGenerator signedDataGenerator = new CMSSignedDataGenerator();
    signedDataGenerator.addSignerInfoGenerator(createSignerInfo(cert));
    signedDataGenerator.addCertificate(createCertificateHolder(cert));

    CMSSignedData singedData = signedDataGenerator.generate(new CMSProcessableByteArray(data), false);
    return Base64.getEncoder().encodeToString(singedData.getEncoded());
  }

  /**
   * Generate the signature bytes based on {@link DiagnosisKeyBatch}.
   *
   * @param batch {@link DiagnosisKeyBatch}.
   * @return signature bytes encoded to Base64.
   * @throws GeneralSecurityException  .
   * @throws CMSException              .
   * @throws OperatorCreationException .
   * @throws IOException               .
   */
  public String createSignatureBytes(DiagnosisKeyBatch batch)
      throws GeneralSecurityException, CMSException, OperatorCreationException, IOException {
    var bytesToSign = this.createBytesToSign(batch);
    return this.sign(bytesToSign, getCertificateFromPublicKey());
  }

  private X509Certificate getCertificateFromPublicKey() {
    return this.cryptoProvider.getCertificate();
  }


}
