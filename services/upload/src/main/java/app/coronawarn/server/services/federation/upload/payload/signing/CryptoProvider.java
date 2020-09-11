/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.federation.upload.payload.signing;

import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class CryptoProvider {

  private final PrivateKey privateKey;
  private final X509Certificate publicKey;

  /**
   * Creates a CryptoProvider, using {@link BouncyCastleProvider}.
   */
  CryptoProvider(ResourceLoader resourceLoader, UploadServiceConfig uploadServiceConfig) throws CertificateException {
    privateKey = loadPrivateKey(resourceLoader, uploadServiceConfig);
    publicKey = loadPublicKey(resourceLoader, uploadServiceConfig);
    Security.addProvider(new BouncyCastleProvider());
  }

  private static PrivateKey getPrivateKeyFromStream(InputStream privateKeyStream)
      throws IOException {
    PEMParser pemParser = new PEMParser(new InputStreamReader(privateKeyStream));
    JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
    var parsedObject = pemParser.readObject();
    if (parsedObject instanceof PEMKeyPair) {
      return converter.getPrivateKey(((PEMKeyPair) parsedObject).getPrivateKeyInfo());
    } else {
      PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(parsedObject);
      return converter.getPrivateKey(privateKeyInfo);
    }
  }

  private X509Certificate loadPublicKey(ResourceLoader resourceLoader,
      UploadServiceConfig uploadServiceConfig) throws CertificateException {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    String path = uploadServiceConfig.getCertificate();
    Resource publicKeyResource = resourceLoader.getResource(path);
    try (InputStream publicKeyStream = publicKeyResource.getInputStream()) {
      return (X509Certificate) cf.generateCertificate(publicKeyStream);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to load private key from " + path, e);
    }
  }

  public X509Certificate getCertificate() {
    return this.publicKey;
  }

  /**
   * Returns the {@link PrivateKey} configured in the application properties.
   */
  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  private PrivateKey loadPrivateKey(ResourceLoader resourceLoader,
      UploadServiceConfig uploadServiceConfig) {
    String path = uploadServiceConfig.getPrivateKey();
    Resource privateKeyResource = resourceLoader.getResource(path);
    try (InputStream privateKeyStream = privateKeyResource.getInputStream()) {
      return getPrivateKeyFromStream(privateKeyStream);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to load private key from " + path, e);
    }
  }
}
