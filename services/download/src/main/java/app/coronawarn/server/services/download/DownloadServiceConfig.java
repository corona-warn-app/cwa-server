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

package app.coronawarn.server.services.download;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "services.download")
@Validated
public class DownloadServiceConfig {

  @Min(0)
  @Max(14)
  private Integer efgsOffsetDays;
  @Min(0)
  @Max(28)
  private Integer retentionDays;

  public Integer getEfgsOffsetDays() {
    return efgsOffsetDays;
  }

  public void setEfgsOffsetDays(Integer efgsOffsetDays) {
    this.efgsOffsetDays = efgsOffsetDays;
  }

  public Integer getRetentionDays() {
    return retentionDays;
  }

  public void setRetentionDays(Integer retentionDays) {
    this.retentionDays = retentionDays;
  }
}
