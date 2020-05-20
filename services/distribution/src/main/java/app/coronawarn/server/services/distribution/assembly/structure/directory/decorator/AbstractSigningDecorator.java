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

package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator;

import app.coronawarn.server.common.protocols.external.exposurenotification.SignatureInfo;
import app.coronawarn.server.common.protocols.external.exposurenotification.TEKSignature;
import app.coronawarn.server.common.protocols.external.exposurenotification.TEKSignatureList;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.archive.ArchiveOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import com.google.protobuf.ByteString;
import java.security.GeneralSecurityException;
import java.security.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link DirectoryDecorator} that will add a signature file to the {@link Directory} it decorates.
 */
public abstract class AbstractSigningDecorator<W extends Writable<W>> extends DirectoryDecorator<W> implements
    SigningDecorator<W> {

  String SIGNATURE_FILE_NAME = "export.sig";
  String SIGNATURE_ALGORITHM = "Ed25519";
  String SECURITY_PROVIDER = "BC";

  private static final Logger logger = LoggerFactory.getLogger(AbstractSigningDecorator.class);
  private final CryptoProvider cryptoProvider;

  public AbstractSigningDecorator(Directory<W> directory, CryptoProvider cryptoProvider) {
    super(directory);
    this.cryptoProvider = cryptoProvider;
  }

  /**
   * See {@link AbstractSigningDecorator} class documentation.
   */
  @Override
  public void prepare(ImmutableStack<Object> indices) {
    super.prepare(indices);
    logger.debug("Adding signature to ..."); // TODO
    // TODO
    TEKSignatureList signatureList = this.createTEKSignatureList(this.cryptoProvider);
    this.addWritable(this.getSignatureFile(SIGNATURE_FILE_NAME, this.cryptoProvider));
    // TODO
    //this.addWritable(new FileOnDisk("export.sig", signatureList.toByteArray()));

    /*
        TODO Continue here tomorrow:
        - Fix abstract decorators
        - Fix "...OnDisk" implementations
        - Try to remove temp directory from zip/archive decorator (only work on byte[])
        - Write unit tests
        - Write integration tests
        - Fix JavaDoc
        - Open draft PR
        - Draft PR deployment to p006
        - Open tickets for:
          - Missing sig algorithm string et al.
          - Missing batching (currently everything 1/1)
          - Missing endpoint reworking
        - Update OpenAPI spec file
        - Update linked tickets
        - Daily reporting
        - Time recording
        - Announce payload updates and new deployment on app channels
     */
  }



  private TEKSignatureList createTEKSignatureList(CryptoProvider cryptoProvider) {
    // TODO
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

  static SignatureInfo getSignatureInfo() {
    return SignatureInfo.newBuilder()
        .setAppBundleId("TODO")
        .setAndroidPackage("TODO")
        .setVerificationKeyVersion("TODO")
        .setVerificationKeyId("TODO")
        .setSignatureAlgorithm("TODO")
        .build();
  }
}
