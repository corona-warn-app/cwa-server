package app.coronawarn.server.common.persistence.domain.config;

public class TransmissionRiskValueMapping {

  private Integer transmissionRiskLevel;
  private Double transmissionRiskValue;

  public TransmissionRiskValueMapping() {
  }

  public Integer getTransmissionRiskLevel() {
    return transmissionRiskLevel;
  }

  public void setTransmissionRiskLevel(Integer transmissionRiskLevel) {
    this.transmissionRiskLevel = transmissionRiskLevel;
  }

  public Double getTransmissionRiskValue() {
    return transmissionRiskValue;
  }

  public void setTransmissionRiskValue(Double transmissionRiskValue) {
    this.transmissionRiskValue = transmissionRiskValue;
  }
}
