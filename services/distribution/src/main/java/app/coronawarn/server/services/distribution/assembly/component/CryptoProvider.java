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
import java.io.UncheckedIOException;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * Wrapper component for a {@link CryptoProvider#getPrivateKey() private key} and a {@link
 * CryptoProvider#getCertificate()} certificate} from the application properties.
 */
@Component
public class CryptoProvider {

  private final PrivateKey privateKey;
  private final Certificate certificate;

  /**
   * Creates a CryptoProvider, using {@link BouncyCastleProvider}.
   */
  CryptoProvider(ResourceLoader resourceLoader, DistributionServiceConfig distributionServiceConfig) {
    privateKey = loadPrivateKey(resourceLoader, distributionServiceConfig);
    certificate = loadCertificate(resourceLoader, distributionServiceConfig);
    Security.addProvider(new BouncyCastleProvider());
  }

  private static PrivateKey getPrivateKeyFromStream(InputStream privateKeyStream) throws IOException {
    InputStreamReader privateKeyStreamReader = new InputStreamReader(privateKeyStream);
    Object parsed = new PEMParser(privateKeyStreamReader).readObject();
    KeyPair pair = new JcaPEMKeyConverter().getKeyPair((PEMKeyPair) parsed);
    return pair.getPrivate();
  }

  private static Certificate getCertificateFromStream(InputStream certificateStream)
      throws IOException, CertificateException {
    return getCertificateFromBytes(certificateStream.readAllBytes());
  }

  private static Certificate getCertificateFromBytes(byte[] bytes)
      throws CertificateException {
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    InputStream certificateByteStream = new ByteArrayInputStream(bytes);
    return certificateFactory.generateCertificate(certificateByteStream);
  }

  /**
   * Returns the {@link PrivateKey} configured in the application properties.
   */
  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  private PrivateKey loadPrivateKey(ResourceLoader resourceLoader,
      DistributionServiceConfig distributionServiceConfig) {
    String path = distributionServiceConfig.getPaths().getPrivateKey();
    Resource privateKeyResource = resourceLoader.getResource(path);
    try (InputStream privateKeyStream = privateKeyResource.getInputStream()) {
      return getPrivateKeyFromStream(privateKeyStream);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to load private key from " + path, e);
    }
  }

  /**
   * Returns the {@link Certificate} configured in the application properties.
   */
  public Certificate getCertificate() {
    return certificate;
  }

  private Certificate loadCertificate(ResourceLoader resourceLoader,
      DistributionServiceConfig distributionServiceConfig) {
    String path = distributionServiceConfig.getPaths().getCertificate();
    Resource certResource = resourceLoader.getResource(path);
    try (InputStream certStream = certResource.getInputStream()) {
      return getCertificateFromStream(certStream);
    } catch (IOException | CertificateException e) {
      throw new RuntimeException("Failed to load certificate from " + path, e);
    }
  }
}
