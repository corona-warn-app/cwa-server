package app.coronawarn.server.services.distribution.dgc;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.file.JsonFile;
import app.coronawarn.server.services.distribution.statistics.file.JsonFileLoader;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"local-json-stats", "processing-test", "debug"})
@ContextConfiguration(classes = {DistributionServiceConfig.class}, initializers = ConfigDataApplicationContextInitializer.class)
public class DigitalGreenCertificateJsonToProtobufTest {

  @MockBean
  private JsonFileLoader jsonFileLoader;

  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

  private static String mahManfJsonPath = "src/test/resources/dgc/vaccine-mah-manf.json";
  private static String mProductJsonPath = "src/test/resources/dgc/vaccine-mah-manf.json";

  @Test
  public void shouldReadMahManfJson() throws IOException {
    JsonFileLoader jsonFileLoader = Mockito.mock(JsonFileLoader.class);
    JsonFile json1 = new JsonFile(new FileInputStream(mahManfJsonPath), "");

    Mockito.when(jsonFileLoader.getFile()).thenReturn(json1);
    DigitalGreenCertificateToProtobufMapping dgcToProtobufMapping =
        new DigitalGreenCertificateToProtobufMapping(distributionServiceConfig, jsonFileLoader, null, null);

    var result = dgcToProtobufMapping.readMahManfJson();

    assertThat(result.getValueSetId()).isEqualTo("vaccines-covid-19-auth-holders");
    assertThat(result.getValueSetDate()).isEqualTo("2021-04-27");
    assertThat(result.getValueSetValues()).hasSize(14);

    // assert at least one value
    ValueSetObject actual = result.getValueSetValues().get("ORG-100001699");
    assertThat(actual.getDisplay()).isEqualTo("AstraZeneca AB");
    assertThat(actual.getLang()).isEqualTo(Language.EN);
    assertThat(actual.isActive()).isEqualTo(true);
    assertThat(actual.getVersion()).isEmpty();
    assertThat(actual.getSystem()).isEqualTo("https://spor.ema.europa.eu/v1/organisations");
  }


  @Test
  public void shouldReadMProductJson() throws IOException {
    JsonFileLoader jsonFileLoader = Mockito.mock(JsonFileLoader.class);
    JsonFile json1 = new JsonFile(new FileInputStream(mProductJsonPath), "");

    Mockito.when(jsonFileLoader.getFile()).thenReturn(json1);
    DigitalGreenCertificateToProtobufMapping dgcToProtobufMapping =
        new DigitalGreenCertificateToProtobufMapping(distributionServiceConfig, jsonFileLoader, null, null);

    var result = dgcToProtobufMapping.readMedicinalProductJson();

    assertThat(result.getValueSetId()).isEqualTo("vaccines-covid-19-names");
    assertThat(result.getValueSetDate()).isEqualTo("2021-04-27");
    assertThat(result.getValueSetValues()).hasSize(12);

    ValueSetObject actual = result.getValueSetValues().get("EU/1/20/1525");
    assertThat(actual.getDisplay()).isEqualTo("COVID-19 Vaccine Janssen");
    assertThat(actual.getLang()).isEqualTo(Language.EN);
    assertThat(actual.isActive()).isEqualTo(true);
    assertThat(actual.getVersion()).isEmpty();
    assertThat(actual.getSystem()).isEqualTo("https://ec.europa.eu/health/documents/community-register/html/");
  }
}
