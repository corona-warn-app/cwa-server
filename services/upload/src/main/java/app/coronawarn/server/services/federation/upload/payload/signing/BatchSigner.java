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
import java.security.cert.CertificateException;
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

  private final CryptoProvider cryptoProvider;

  private final UploadServiceConfig uploadServiceConfig;

  public BatchSigner(CryptoProvider cryptoProvider,
      UploadServiceConfig uploadServiceConfig) {
    this.cryptoProvider = cryptoProvider;
    this.uploadServiceConfig = uploadServiceConfig;
  }

  private static List<DiagnosisKey> sortBatchByKeyData(DiagnosisKeyBatch batch) {
    return batch.getKeysList()
        .stream()
        .sorted(Comparator.comparing(diagnosisKey -> diagnosisKey.getKeyData().toStringUtf8()))
        .collect(Collectors.toList());
  }

  private byte[] createBytesToSign(final DiagnosisKeyBatch batch) {
    final ByteArrayOutputStream batchBytes = new ByteArrayOutputStream();
    final List<DiagnosisKey> sortedBatch = sortBatchByKeyData(batch);
    for (DiagnosisKey diagnosisKey : sortedBatch) {
      batchBytes.writeBytes(diagnosisKey.getKeyData().toStringUtf8().getBytes(StandardCharsets.UTF_8));
      batchBytes.writeBytes(ByteBuffer.allocate(4).putInt(diagnosisKey.getRollingStartIntervalNumber()).array());
      batchBytes.writeBytes(ByteBuffer.allocate(4).putInt(diagnosisKey.getRollingPeriod()).array());
      batchBytes.writeBytes(ByteBuffer.allocate(4).putInt(diagnosisKey.getTransmissionRiskLevel()).array());

      diagnosisKey.getVisitedCountriesList()
          .forEach(country -> batchBytes.writeBytes(country.getBytes(StandardCharsets.UTF_8)));

      batchBytes.writeBytes(diagnosisKey.getOrigin().getBytes(StandardCharsets.UTF_8));
      batchBytes.writeBytes(ByteBuffer.allocate(4).putInt(diagnosisKey.getReportTypeValue()).array());
      batchBytes.writeBytes(ByteBuffer.allocate(4).putInt(diagnosisKey.getDaysSinceOnsetOfSymptoms()).array());
    }
    return batchBytes.toByteArray();
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
   * TODO: Create SignatureException Generate the signature bytes based on {@link DiagnosisKeyBatch}.
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

  private X509Certificate getCertificateFromPublicKey() throws CertificateException {
    return this.cryptoProvider.getCertificate();
  }



}
