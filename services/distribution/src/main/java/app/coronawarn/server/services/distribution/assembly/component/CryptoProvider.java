/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * Wrapper component for a {@link CryptoProvider#getPrivateKey() private key} and a {@link
 * CryptoProvider#getCertificate()} certificate} from the application properties.
 */
@Component
public class CryptoProvider {

  private static final Logger logger = LoggerFactory.getLogger(CryptoProvider.class);

  private final String privateKeyPath;

  private final String certificatePath;

  private final ResourceLoader resourceLoader;

  private PrivateKey privateKey;
  private Certificate certificate;

  /**
   * Creates a CryptoProvider, using {@link BouncyCastleProvider}.
   */
  CryptoProvider(ResourceLoader resourceLoader, DistributionServiceConfig distributionServiceConfig) {
    this.resourceLoader = resourceLoader;
    this.privateKeyPath = distributionServiceConfig.getPaths().getPrivateKey();
    this.certificatePath = distributionServiceConfig.getPaths().getCertificate();
    Security.addProvider(new BouncyCastleProvider());
  }

  private static PrivateKey getPrivateKeyFromStream(final InputStream privateKeyStream) throws IOException {
    InputStreamReader privateKeyStreamReader = new InputStreamReader(privateKeyStream);
    Object parsed = new PEMParser(privateKeyStreamReader).readObject();
    KeyPair pair = new JcaPEMKeyConverter().getKeyPair((PEMKeyPair) parsed);
    return pair.getPrivate();
  }

  private static Certificate getCertificateFromStream(final InputStream certificateStream)
      throws IOException, CertificateException {
    return getCertificateFromBytes(certificateStream.readAllBytes());
  }

  private static Certificate getCertificateFromBytes(final byte[] bytes)
      throws CertificateException {
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    InputStream certificateByteStream = new ByteArrayInputStream(bytes);
    return certificateFactory.generateCertificate(certificateByteStream);
  }

  /**
   * Reads and returns the {@link PrivateKey} configured in the application properties.
   */
  public PrivateKey getPrivateKey() {
    if (privateKey == null) {
      Resource privateKeyResource = resourceLoader.getResource(privateKeyPath);
      try (InputStream privateKeyStream = privateKeyResource.getInputStream()) {
        privateKey = getPrivateKeyFromStream(privateKeyStream);
      } catch (IOException e) {
        logger.error("Failed to load private key from {}", privateKeyPath, e);
        throw new RuntimeException(e);
      }
    }
    return privateKey;
  }

  /**
   * Reads and returns the {@link Certificate} configured in the application properties.
   */
  public Certificate getCertificate() {
    if (this.certificate == null) {
      Resource certResource = resourceLoader.getResource(certificatePath);
      try (InputStream certStream = certResource.getInputStream()) {
        this.certificate = getCertificateFromStream(certStream);
      } catch (IOException | CertificateException e) {
        logger.error("Failed to load certificate from {}", certificatePath, e);
        throw new RuntimeException(e);
      }
    }
    return certificate;
  }
}
