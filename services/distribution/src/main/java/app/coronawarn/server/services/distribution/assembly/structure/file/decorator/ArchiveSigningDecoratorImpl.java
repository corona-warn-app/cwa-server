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

package app.coronawarn.server.services.distribution.assembly.structure.file.decorator;

import app.coronawarn.server.common.protocols.external.exposurenotification.SignatureInfo;
import app.coronawarn.server.common.protocols.external.exposurenotification.TEKSignature;
import app.coronawarn.server.common.protocols.external.exposurenotification.TEKSignatureList;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.file.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileImpl;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import com.google.protobuf.ByteString;
import java.security.GeneralSecurityException;
import java.security.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link ArchiveDecorator} that will add a signature to the {@link Archive} it decorates. The
 * decorated archive may only contain one writable.
 */
public abstract class ArchiveSigningDecoratorImpl extends ArchiveDecorator implements
    ArchiveSigningDecorator {

  private static final String SIGNATURE_ALGORITHM = "Ed25519";
  private static final String SECURITY_PROVIDER = "BC";

  private static final Logger logger = LoggerFactory.getLogger(ArchiveSigningDecoratorImpl.class);
  private final CryptoProvider cryptoProvider;

  public ArchiveSigningDecoratorImpl(Archive archive, CryptoProvider cryptoProvider) {
    super(archive);
    this.cryptoProvider = cryptoProvider;
  }

  /**
   * See {@link ArchiveSigningDecoratorImpl} class documentation.
   */
  @Override
  public void prepare(ImmutableStack<Object> indices) {
    super.prepare(indices);
    logger.debug("Adding signature to {}", this.getFileOnDisk().getPath());
    TEKSignatureList signatureList = this.createTEKSignatureList();
    this.addWritable(new FileImpl("export.sig", signatureList.toByteArray()));
  }

  private TEKSignatureList createTEKSignatureList() {
    // TODO
    return TEKSignatureList.newBuilder()
        .addSignatures(TEKSignature.newBuilder()
            .setSignatureInfo(getSignatureInfo())
            .setBatchNum(this.getBatchNum())
            .setBatchSize(this.getBatchSize())
            .setSignature(ByteString.copyFrom(this.createSignature()))
            .build())
        .build();
  }

  private byte[] createSignature() {
    try {
      Signature payloadSignature = Signature.getInstance(SIGNATURE_ALGORITHM, SECURITY_PROVIDER);
      payloadSignature.initSign(this.cryptoProvider.getPrivateKey());
      payloadSignature.update(this.getBytesToSign());
      return payloadSignature.sign();
    } catch (GeneralSecurityException e) {
      logger.error("Failed to sign archive.", e);
      throw new RuntimeException(e);
    }
  }

  public static SignatureInfo getSignatureInfo() {
    return SignatureInfo.newBuilder()
        .setAppBundleId("TODO")
        .setAndroidPackage("TODO")
        .setVerificationKeyVersion("TODO")
        .setVerificationKeyId("TODO")
        .setSignatureAlgorithm("TODO")
        .build();
  }
}
