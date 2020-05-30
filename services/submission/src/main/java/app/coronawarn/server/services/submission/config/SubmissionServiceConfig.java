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

package app.coronawarn.server.services.submission.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "services.submission")
public class SubmissionServiceConfig {

  // Exponential moving average of the last N real request durations (in ms), where
  // N = fakeDelayMovingAverageSamples.
  private Double initialFakeDelayMilliseconds;
  private Double fakeDelayMovingAverageSamples;
  private Integer retentionDays;
  private Payload payload;
  private Verification verification;

  public Double getInitialFakeDelayMilliseconds() {
    return initialFakeDelayMilliseconds;
  }

  public void setInitialFakeDelayMilliseconds(Double initialFakeDelayMilliseconds) {
    this.initialFakeDelayMilliseconds = initialFakeDelayMilliseconds;
  }

  public Double getFakeDelayMovingAverageSamples() {
    return fakeDelayMovingAverageSamples;
  }

  public void setFakeDelayMovingAverageSamples(Double fakeDelayMovingAverageSamples) {
    this.fakeDelayMovingAverageSamples = fakeDelayMovingAverageSamples;
  }

  public Integer getRetentionDays() {
    return retentionDays;
  }

  public void setRetentionDays(Integer retentionDays) {
    this.retentionDays = retentionDays;
  }

  public Integer getMaxNumberOfKeys() {
    return payload.getMaxNumberOfKeys();
  }

  public void setPayload(Payload payload) {
    this.payload = payload;
  }

  private static class Payload {

    private Integer maxNumberOfKeys;

    public Integer getMaxNumberOfKeys() {
      return maxNumberOfKeys;
    }

    public void setMaxNumberOfKeys(Integer maxNumberOfKeys) {
      this.maxNumberOfKeys = maxNumberOfKeys;
    }
  }

  public String getVerificationBaseUrl() {
    return verification.getBaseUrl();
  }

  public void setVerification(Verification verification) {
    this.verification = verification;
  }

  public String getVerificationPath() {
    return verification.getPath();
  }

  private static class Verification {

    private String baseUrl;

    private String path;

    public String getBaseUrl() {
      return baseUrl;
    }

    public String getPath() {
      return path;
    }

    public void setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
    }

    public void setPath(String path) {
      this.path = path;
    }
  }

}
