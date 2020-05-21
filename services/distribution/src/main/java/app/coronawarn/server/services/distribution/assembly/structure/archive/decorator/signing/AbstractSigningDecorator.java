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

package app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing;

import app.coronawarn.server.common.protocols.external.exposurenotification.SignatureInfo;
import app.coronawarn.server.common.protocols.external.exposurenotification.TEKSignature;
import app.coronawarn.server.common.protocols.external.exposurenotification.TEKSignatureList;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.ArchiveDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import com.google.protobuf.ByteString;
import java.security.GeneralSecurityException;
import java.security.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSigningDecorator<W extends Writable<W>> extends ArchiveDecorator<W> implements
    SigningDecorator<W> {

  private static final String SIGNATURE_FILE_NAME = "export.sig";
  private static final String SIGNATURE_ALGORITHM = "Ed25519";
  private static final String SECURITY_PROVIDER = "BC";

  private static final Logger logger = LoggerFactory.getLogger(AbstractSigningDecorator.class);
  protected final CryptoProvider cryptoProvider;

  public AbstractSigningDecorator(Archive<W> archive, CryptoProvider cryptoProvider) {
    super(archive);
    this.cryptoProvider = cryptoProvider;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    super.prepare(indices);
    this.addWritable(this.getSignatureFile(SIGNATURE_FILE_NAME));
  }

  protected TEKSignatureList createTemporaryExposureKeySignatureList(CryptoProvider cryptoProvider) {
    return TEKSignatureList.newBuilder()
        .addSignatures(TEKSignature.newBuilder()
            .setSignatureInfo(getSignatureInfo())
            .setBatchNum(getBatchNum())
            .setBatchSize(getBatchSize())
            .setSignature(ByteString.copyFrom(createSignature(cryptoProvider)))
            .build())
        .build();
  }

  private byte[] createSignature(CryptoProvider cryptoProvider) {
    try {
      Signature payloadSignature = Signature.getInstance(SIGNATURE_ALGORITHM, SECURITY_PROVIDER);
      payloadSignature.initSign(cryptoProvider.getPrivateKey());
      payloadSignature.update(this.getBytesToSign());
      return payloadSignature.sign();
    } catch (GeneralSecurityException e) {
      logger.error("Failed to sign archive.", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns the static {@link SignatureInfo} configured in the application properties. TODO Enter correct values.
   */
  public static SignatureInfo getSignatureInfo() {
    // TODO cwa-server#205, cwa-server#206
    return SignatureInfo.newBuilder()
        .setAppBundleId("TODO")
        .setAndroidPackage("TODO")
        .setVerificationKeyVersion("TODO")
        .setVerificationKeyId("TODO")
        .setSignatureAlgorithm("TODO")
        .build();
  }
}
