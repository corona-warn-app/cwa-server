/*
 * Corona-Warn-App
 *
 * Deutsche Telekom AG, SAP SE and all other contributors /
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

package app.coronawarn.server.services.distribution.assembly.structure.file.decorator;

import app.coronawarn.server.common.protocols.internal.SignedPayload;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import com.google.protobuf.ByteString;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link FileDecorator} that will convert the contents of its {@link File} into a {@link
 * app.coronawarn.server.common.protocols.internal.SignedPayload}.
 */
public class SigningDecorator extends FileDecorator {

  private static final String SIGNATURE_ALGORITHM = "Ed25519";
  private static final String SECURITY_PROVIDER = "BC";

  private static final Logger logger = LoggerFactory.getLogger(SigningDecorator.class);
  private final CryptoProvider cryptoProvider;

  public SigningDecorator(File file, CryptoProvider cryptoProvider) {
    super(file);
    this.cryptoProvider = cryptoProvider;
  }

  /**
   * See {@link SigningDecorator} class documentation.
   */
  @Override
  public void prepare(ImmutableStack<Object> indices) {
    super.prepare(indices);
    logger.debug("Signing {}", this.getFileOnDisk().getPath());
    SignedPayload signedPayload = sign(this.getBytes(), cryptoProvider.getPrivateKey(),
        cryptoProvider.getCertificate());
    this.setBytes(signedPayload.toByteArray());
  }

  private static SignedPayload sign(byte[] payload, PrivateKey privateKey,
      Certificate certificate) {
    try {
      Signature payloadSignature = Signature.getInstance(SIGNATURE_ALGORITHM, SECURITY_PROVIDER);
      payloadSignature.initSign(privateKey);
      payloadSignature.update(payload);
      return SignedPayload.newBuilder()
          .setCertificateChain(ByteString.copyFrom(certificate.getEncoded()))
          .setSignature(ByteString.copyFrom(payloadSignature.sign()))
          .setPayload(ByteString.copyFrom(payload))
          .build();
    } catch (GeneralSecurityException e) {
      logger.error("Exception during payload signing.", e);
      throw new RuntimeException(e);
    }
  }
}
