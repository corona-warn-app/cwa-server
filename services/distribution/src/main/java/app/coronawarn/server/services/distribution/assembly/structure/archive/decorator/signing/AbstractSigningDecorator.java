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

package app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing;

import app.coronawarn.server.common.protocols.external.exposurenotification.TEKSignature;
import app.coronawarn.server.common.protocols.external.exposurenotification.TEKSignatureList;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.ArchiveDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import com.google.protobuf.ByteString;
import java.security.GeneralSecurityException;
import java.security.Signature;

public abstract class AbstractSigningDecorator<W extends Writable<W>> extends ArchiveDecorator<W>
    implements SigningDecorator<W> {

  protected final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Creates an AbstractSigningDecorator.
   */
  public AbstractSigningDecorator(Archive<W> archive, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    super(archive);
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    super.prepare(indices);
    this.addWritable(this.getSignatureFile(distributionServiceConfig.getSignature().getFileName()));
  }

  protected TEKSignatureList createTemporaryExposureKeySignatureList() {
    return TEKSignatureList.newBuilder()
        .addSignatures(TEKSignature.newBuilder()
            .setSignatureInfo(distributionServiceConfig.getSignature().getSignatureInfo())
            .setBatchNum(getBatchNum())
            .setBatchSize(getBatchSize())
            .setSignature(ByteString.copyFrom(createSignature(cryptoProvider)))
            .build())
        .build();
  }

  private byte[] createSignature(CryptoProvider cryptoProvider) {
    try {
      Signature payloadSignature = Signature.getInstance(distributionServiceConfig.getSignature().getAlgorithmName(),
          distributionServiceConfig.getSignature().getSecurityProvider());
      payloadSignature.initSign(cryptoProvider.getPrivateKey());
      payloadSignature.update(this.getBytesToSign());
      return payloadSignature.sign();
    } catch (GeneralSecurityException e) {
      throw new RuntimeException("Failed to sign archive.", e);
    }
  }
}
