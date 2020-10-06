

package app.coronawarn.server.services.federation.upload.payload.signing;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BatchSigner {

  private static final Logger logger = LoggerFactory
      .getLogger(BatchSigner.class);

  private final CryptoProvider cryptoProvider;

  private final UploadServiceConfig uploadServiceConfig;

  public BatchSigner(CryptoProvider cryptoProvider,
      UploadServiceConfig uploadServiceConfig) {
    this.cryptoProvider = cryptoProvider;
    this.uploadServiceConfig = uploadServiceConfig;
  }

  private byte[] bytesToBase64(byte[] bytes) {
    try {
      return Base64.getEncoder()
          .encodeToString(bytes)
          .getBytes(StandardCharsets.US_ASCII);
    } catch (IllegalArgumentException e) {
      logger.error("Failed to convert byte array to Base64");
      return null;
    }
  }

  private byte[] createBytesToSign(final DiagnosisKeyBatch batch) {
    final ByteArrayOutputStream batchBytes = new ByteArrayOutputStream();
    for (DiagnosisKey diagnosisKey : sortBatchByKeyData(batch)) {
      batchBytes.writeBytes(bytesToBase64(diagnosisKey.getKeyData().toStringUtf8().getBytes(StandardCharsets.UTF_8)));
      batchBytes.writeBytes(".".getBytes(StandardCharsets.US_ASCII));
      batchBytes.writeBytes(bytesToBase64(ByteBuffer.allocate(4)
          .putInt(diagnosisKey.getRollingStartIntervalNumber()).array()));
      batchBytes.writeBytes(".".getBytes(StandardCharsets.US_ASCII));
      batchBytes.writeBytes(bytesToBase64(ByteBuffer.allocate(4)
          .putInt(diagnosisKey.getRollingPeriod()).array()));
      batchBytes.writeBytes(".".getBytes(StandardCharsets.US_ASCII));
      batchBytes.writeBytes(bytesToBase64(ByteBuffer.allocate(4)
          .putInt(diagnosisKey.getTransmissionRiskLevel()).array()));
      batchBytes.writeBytes(".".getBytes(StandardCharsets.US_ASCII));

      String countries = diagnosisKey.getVisitedCountriesList()
          .stream().sorted(String::compareTo).collect(Collectors.joining(","));
      batchBytes.writeBytes(bytesToBase64(countries.getBytes()));

      batchBytes.writeBytes(bytesToBase64(diagnosisKey.getOrigin().getBytes(StandardCharsets.UTF_8)));
      batchBytes.writeBytes(".".getBytes(StandardCharsets.US_ASCII));
      batchBytes.writeBytes(bytesToBase64(ByteBuffer.allocate(4)
          .putInt(diagnosisKey.getReportType().getNumber()).array()));
      batchBytes.writeBytes(".".getBytes(StandardCharsets.US_ASCII));
      batchBytes.writeBytes(bytesToBase64(ByteBuffer.allocate(4)
          .putInt(diagnosisKey.getDaysSinceOnsetOfSymptoms()).array()));
      batchBytes.writeBytes(".".getBytes(StandardCharsets.US_ASCII));
    }
    return batchBytes.toByteArray();
  }

  private static List<app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey>
        sortBatchByKeyData(DiagnosisKeyBatch batch) {
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
