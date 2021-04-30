package app.coronawarn.server.services.distribution.dgc;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.DigitalGreenCertificate;
import java.text.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"local-json-stats", "processing-test", "debug"})
@ContextConfiguration(classes = {DistributionServiceConfig.class},
    initializers = ConfigDataApplicationContextInitializer.class)
class DigitalGreenCertificateJsonToProtobufTest {

  @Autowired
  private DistributionServiceConfig distributionServiceConfig;

  private static final String mahJsonPath = "src/test/resources/dgc/vaccine-mah.json";
  private static final String mProductJsonPath = "src/test/resources/dgc/vaccine-medicinal-product.json";
  private static final String prophylaxisJsonPath = "src/test/resources/dgc/vaccine-prophylaxis.json";

  @BeforeEach
  void setUp() {
    distributionServiceConfig.setDigitalGreenCertificate(Mockito.mock(DigitalGreenCertificate.class));
  }

  @Test
  void shouldReadMahJson() throws ParseException {
    distributionServiceConfig.getDigitalGreenCertificate().setMahPath(mahJsonPath);
    DigitalGreenCertificateToProtobufMapping dgcToProtobufMapping =
        new DigitalGreenCertificateToProtobufMapping(distributionServiceConfig);

    var result = dgcToProtobufMapping.readMahJson();

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
  void shouldReadMProductJson() throws ParseException {
    distributionServiceConfig.getDigitalGreenCertificate().setMedicinalProductsPath(mProductJsonPath);
    DigitalGreenCertificateToProtobufMapping dgcToProtobufMapping =
        new DigitalGreenCertificateToProtobufMapping(distributionServiceConfig);

    var result = dgcToProtobufMapping.readMedicinalProductJson();

    assertThat(result.getValueSetId()).isEqualTo("vaccines-covid-19-names");
    assertThat(result.getValueSetDate()).isEqualTo("2021-04-27");
    assertThat(result.getValueSetValues()).hasSize(12);

    ValueSetObject actual = result.getValueSetValues().get(("EU/1/20/1525"));
    assertThat(actual.getDisplay()).isEqualTo("COVID-19 Vaccine Janssen");
    assertThat(actual.getLang()).isEqualTo(Language.EN);
    assertThat(actual.isActive()).isEqualTo(true);
    assertThat(actual.getVersion()).isEmpty();
    assertThat(actual.getSystem()).isEqualTo("https://ec.europa.eu/health/documents/community-register/html/");
  }

  @Test
  void shouldReadProphylaxisJson() throws ParseException {
    distributionServiceConfig.getDigitalGreenCertificate().setProphylaxisPath(prophylaxisJsonPath);
    DigitalGreenCertificateToProtobufMapping dgcToProtobufMapping =
        new DigitalGreenCertificateToProtobufMapping(distributionServiceConfig);

    var result = dgcToProtobufMapping.readProphylaxisJson();

    assertThat(result.getValueSetId()).isEqualTo("sct-vaccines-covid-19");
    assertThat(result.getValueSetDate()).isEqualTo("2021-04-27");
    assertThat(result.getValueSetValues()).hasSize(3);

    // assert at least one value
    ValueSetObject actual = result.getValueSetValues().get("1119305005");
    assertThat(actual.getDisplay()).isEqualTo("SARS-CoV-2 antigen vaccine");
    assertThat(actual.getLang()).isEqualTo(Language.EN);
    assertThat(actual.isActive()).isEqualTo(true);
    assertThat(actual.getVersion()).isEqualTo("http://snomed.info/sct/900000000000207008/version/20210131");
    assertThat(actual.getSystem()).isEqualTo("http://snomed.info/sct");
  }
}
