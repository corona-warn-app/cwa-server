package app.coronawarn.server.services.submission.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * Convenient wrapper over cryptographic operations performed in the Submission service.
 */
@Component
public class CryptoProvider {

  private final X509Certificate publicKey;
  private final String signatureAlgorithm;

  /**
   * Creates an instance wrapping a {@link BouncyCastleProvider}.
   */
  CryptoProvider(ResourceLoader resourceLoader, SubmissionServiceConfig submissionServiceConfig)
      throws CertificateException {
    this.publicKey = loadPublicKey(resourceLoader, submissionServiceConfig);
    this.signatureAlgorithm = submissionServiceConfig.getSignatureAlgorithm();
    Security.addProvider(new BouncyCastleProvider());
  }

  private X509Certificate loadPublicKey(ResourceLoader resourceLoader,
      SubmissionServiceConfig submissionServiceConfig) throws CertificateException {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    String path = submissionServiceConfig.getSignatureVerificationKey();
    Resource publicKeyResource = resourceLoader.getResource(path);
    try (InputStream publicKeyStream = publicKeyResource.getInputStream()) {
      return (X509Certificate) cf.generateCertificate(publicKeyStream);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to load public key from " + path
          + " in the context of verifying TraceLocation data signatures", e);
    }
  }

  public String getSignatureAlgorithm() {
    return this.signatureAlgorithm;
  }

  public X509Certificate getCertificate() {
    return this.publicKey;
  }
}
