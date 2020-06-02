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

import app.coronawarn.server.common.protocols.external.exposurenotification.TEKSignatureList;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;

public abstract class SigningDecoratorOnDisk extends AbstractSigningDecorator<WritableOnDisk> implements
    SigningDecorator<WritableOnDisk> {

  public SigningDecoratorOnDisk(Archive<WritableOnDisk> archive, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig) {
    super(archive, cryptoProvider, distributionServiceConfig);
  }

  @Override
  public FileOnDisk getSignatureFile(String signatureFileName) {
    TEKSignatureList signatureList = this.createTemporaryExposureKeySignatureList();
    return new FileOnDisk(signatureFileName, signatureList.toByteArray());
  }
}
