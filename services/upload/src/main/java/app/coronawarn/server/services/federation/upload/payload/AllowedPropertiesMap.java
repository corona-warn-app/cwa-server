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

package app.coronawarn.server.services.federation.upload.payload;

import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import org.springframework.stereotype.Component;

@Component
public class AllowedPropertiesMap {

  private final boolean canSendDsos;
  private final boolean canSendReportType;
  private final Integer defaultDsos;
  private final ReportType defaultReportType;

  /**
   * Reads configuration and returns the received value if properties are allowed to be sent to EFGS. Returns a default
   * otherwise.
   * @param configuration {@link UploadServiceConfig} Upload configuration.
   */
  public AllowedPropertiesMap(UploadServiceConfig configuration) {
    this.canSendDsos = configuration.getEfgsTransmission().isEnableDsos();
    this.canSendReportType = configuration.getEfgsTransmission().isEnableReportType();
    this.defaultDsos = configuration.getEfgsTransmission().getDefaultDsos();
    this.defaultReportType = ReportType.UNKNOWN;
  }

  public int getDsosIfAllowed(int dsos) {
    return this.canSendDsos ? dsos : this.defaultDsos;
  }

  public ReportType getReportTypeIfAllowed(ReportType type) {
    return this.canSendReportType ? type : this.defaultReportType;
  }

}
