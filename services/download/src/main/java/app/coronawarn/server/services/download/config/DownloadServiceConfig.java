

package app.coronawarn.server.services.download.config;

import java.util.Map;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
  private TekFieldDerivations tekFieldDerivations;

  public TekFieldDerivations getTekFieldDerivations() {
    return tekFieldDerivations;
  }

  public void setTekFieldDerivations(TekFieldDerivations tekFieldDerivations) {
    this.tekFieldDerivations = tekFieldDerivations;
  }

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

  public static class TekFieldDerivations {

    @NotNull
    @NotEmpty
    private Map<Integer, Integer> trlFromDsos;

    public Map<Integer, Integer> getTransmissionRiskLevelFromDaysSinceOnsetOfSymptoms() {
      return trlFromDsos;
    }

    public void setTrlFromDsos(Map<Integer, Integer> trlFromDsos) {
      this.trlFromDsos = trlFromDsos;
    }

    public Integer deriveTrlFromDsos(Integer dsos) {
      return trlFromDsos.getOrDefault(dsos, 1);
    }
  }
}
