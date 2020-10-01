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

package app.coronawarn.server.services.federation.upload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services.upload")
public class UploadServiceConfig {

  private Integer expiryPolicyMinutes;
  private Integer minBatchKeyCount;
  private Integer maxBatchKeyCount;
  private String privateKey;
  private String privateKeyPassword;
  private String certificate;
  private Signature signature;
  private TestData testData;
  private EfgsTransmission efgsTransmission;

  public EfgsTransmission getEfgsTransmission() {
    return efgsTransmission;
  }

  public void setEfgsTransmission(EfgsTransmission efgsTransmission) {
    this.efgsTransmission = efgsTransmission;
  }

  public Integer getExpiryPolicyMinutes() {
    return expiryPolicyMinutes;
  }

  public void setExpiryPolicyMinutes(Integer expiryPolicyMinutes) {
    this.expiryPolicyMinutes = expiryPolicyMinutes;
  }

  public Integer getMinBatchKeyCount() {
    return minBatchKeyCount;
  }

  public void setMinBatchKeyCount(Integer minBatchKeyCount) {
    this.minBatchKeyCount = minBatchKeyCount;
  }

  public Integer getMaxBatchKeyCount() {
    return maxBatchKeyCount;
  }

  public void setMaxBatchKeyCount(Integer maxBatchKeyCount) {
    this.maxBatchKeyCount = maxBatchKeyCount;
  }

  public String getCertificate() {
    return certificate;
  }

  public TestData getTestData() {
    return testData;
  }

  public void setTestData(TestData testData) {
    this.testData = testData;
  }

  public void setCertificate(String certificate) {
    this.certificate = certificate;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }

  public String getPrivateKeyPassword() {
    return privateKeyPassword;
  }

  public void setPrivateKeyPassword(String privateKeyPassword) {
    this.privateKeyPassword = privateKeyPassword;
  }

  public Signature getSignature() {
    return signature;
  }

  public void setSignature(Signature signature) {
    this.signature = signature;
  }

  public static class EfgsTransmission {
    private boolean enableDsos;
    private boolean enableReportType;
    private Integer defaultDsos;

    public boolean isEnableDsos() {
      return enableDsos;
    }

    public void setEnableDsos(boolean enableDsos) {
      this.enableDsos = enableDsos;
    }

    public boolean isEnableReportType() {
      return enableReportType;
    }

    public void setEnableReportType(boolean enableReportType) {
      this.enableReportType = enableReportType;
    }

    public Integer getDefaultDsos() {
      return defaultDsos;
    }

    public void setDefaultDsos(Integer defaultDsos) {
      this.defaultDsos = defaultDsos;
    }
  }

  public static class TestData {
    private int keysPerHour;

    public int getKeysPerHour() {
      return keysPerHour;
    }

    public void setKeysPerHour(int keysPerHour) {
      this.keysPerHour = keysPerHour;
    }
  }

  public static class Signature {

    private String algorithmOid;
    private String algorithmName;
    private String securityProvider;

    public String getAlgorithmOid() {
      return algorithmOid;
    }

    public void setAlgorithmOid(String algorithmOid) {
      this.algorithmOid = algorithmOid;
    }

    public String getAlgorithmName() {
      return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
      this.algorithmName = algorithmName;
    }

    public String getSecurityProvider() {
      return securityProvider;
    }

    public void setSecurityProvider(String securityProvider) {
      this.securityProvider = securityProvider;
    }
  }
}
