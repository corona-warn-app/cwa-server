package app.coronawarn.server.services.distribution.dgc;

import java.util.Date;

public class CertificateStructure {

  private String certificateType;
  private String country;
  private String kid;
  private String rawData;
  private String signature;
  private String thumbprint;
  private Date timestamp;

  public String getCertificateType() {
    return certificateType;
  }

  public void setCertificateType(String certificateType) {
    this.certificateType = certificateType;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getKid() {
    return kid;
  }

  public void setKid(String kid) {
    this.kid = kid;
  }

  public String getRawData() {
    return rawData;
  }

  public void setRawData(String rawData) {
    this.rawData = rawData;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  public String getThumbprint() {
    return thumbprint;
  }

  public void setThumbprint(String thumbprint) {
    this.thumbprint = thumbprint;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }
}
