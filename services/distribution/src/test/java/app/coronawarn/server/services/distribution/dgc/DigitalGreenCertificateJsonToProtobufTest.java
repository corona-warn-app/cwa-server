package app.coronawarn.server.services.distribution.dgc;

import static app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping.DISEASE_AGENT_TARGETED_ID;
import static app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping.TEST_MANF_ID;
import static app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping.TEST_RESULT_ID;
import static app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping.TEST_TYPE_ID;
import static app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping.VACCINE_MAH_ID;
import static app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping.VACCINE_MEDICINAL_PRODUCT_ID;
import static app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToProtobufMapping.VACCINE_PROPHYLAXIS_ID;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, DigitalGreenCertificateToProtobufMapping.class,
    TestDigitalCovidCertificateClient.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("fake-dcc-client")
class DigitalGreenCertificateJsonToProtobufTest {

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  DigitalGreenCertificateToProtobufMapping dgcToProtobufMapping;

  @BeforeEach
  void setup(){
    dgcToProtobufMapping.dccClient = new TestDigitalCovidCertificateClient(dgcToProtobufMapping.resourceLoader) {
      @Override
      public List<ValueSetMetadata> getValueSets() {
        return Collections.emptyList();
      }
    };
  }

  @Test
  void shouldReadDefaultMahJsonIfNotConfigured() throws UnableToLoadFileException {
    var result = dgcToProtobufMapping.readMahJson();

    assertThat(result.getValueSetId()).isEqualTo(VACCINE_MAH_ID);
    assertThat(result.getValueSetDate()).isEqualTo("2021-04-27");
    assertThat(result.getValueSetValues()).hasSize(14);

    // assert at least one value
    ValueSetObject actual = result.getValueSetValues().get("ORG-100001699");
    assertThat(actual.getDisplay()).isEqualTo("AstraZeneca AB");
    assertThat(actual.getLang()).isEqualTo("en");
    assertThat(actual.isActive()).isTrue();
    assertThat(actual.getVersion()).isEmpty();
    assertThat(actual.getSystem()).isEqualTo("https://spor.ema.europa.eu/v1/organisations");
  }


  @Test
  void shouldReadDefaultMProductJsonIfNotConfigured() throws UnableToLoadFileException {
    var result = dgcToProtobufMapping.readMedicinalProductJson();

    assertThat(result.getValueSetId()).isEqualTo(VACCINE_MEDICINAL_PRODUCT_ID);
    assertThat(result.getValueSetDate()).isEqualTo("2021-04-27");
    assertThat(result.getValueSetValues()).hasSize(12);

    // assert at least one value
    ValueSetObject actual = result.getValueSetValues().get(("EU/1/20/1525"));
    assertThat(actual.getDisplay()).isEqualTo("COVID-19 Vaccine Janssen");
    assertThat(actual.getLang()).isEqualTo("en");
    assertThat(actual.isActive()).isTrue();
    assertThat(actual.getVersion()).isEmpty();
    assertThat(actual.getSystem()).isEqualTo("https://ec.europa.eu/health/documents/community-register/html/");
  }

  @Test
  void shouldReadDefaultProphylaxisJsonIfNotConfigured() throws UnableToLoadFileException {
    var result = dgcToProtobufMapping.readProphylaxisJson();

    assertThat(result.getValueSetId()).isEqualTo(VACCINE_PROPHYLAXIS_ID);
    assertThat(result.getValueSetDate()).isEqualTo("2021-04-27");
    assertThat(result.getValueSetValues()).hasSize(3);

    // assert at least one value
    ValueSetObject actual = result.getValueSetValues().get("1119305005");
    assertThat(actual.getDisplay()).isEqualTo("SARS-CoV-2 antigen vaccine");
    assertThat(actual.getLang()).isEqualTo("en");
    assertThat(actual.isActive()).isTrue();
    assertThat(actual.getVersion()).isEqualTo("http://snomed.info/sct/900000000000207008/version/20210131");
    assertThat(actual.getSystem()).isEqualTo("http://snomed.info/sct");
  }

  @Test
  @DirtiesContext
  void shouldReadConfiguredProphylaxisJson() throws UnableToLoadFileException {
    distributionServiceConfig.getDigitalGreenCertificate().setProphylaxisJsonPath("dgc/vaccine-prophylaxis-test.json");
    var result = dgcToProtobufMapping.readProphylaxisJson();

    assertThat(result.getValueSetId()).isEqualTo("sct-vaccines-covid-21");
    assertThat(result.getValueSetDate()).isEqualTo("2021-05-10");
    assertThat(result.getValueSetValues()).hasSize(2);

    // assert at least one value
    ValueSetObject actual = result.getValueSetValues().get("11193050094");
    assertThat(actual.getDisplay()).isEqualTo("SARS-CoV-2 antigen Vaccine");
    assertThat(actual.getLang()).isEqualTo("es");
    assertThat(actual.isActive()).isTrue();
    assertThat(actual.getVersion()).isEqualTo("http://snomed.info/sct/900000000000207008/version/20210131");
    assertThat(actual.getSystem()).isEqualTo("http://snomed.info/sct");
  }

  @Test
  void shouldReadDefaultTestManfJsonIfNotConfigured() throws UnableToLoadFileException {
    var result = dgcToProtobufMapping.readTestManfJson();

    assertThat(result.getValueSetId()).isEqualTo(TEST_MANF_ID);
    assertThat(result.getValueSetDate()).isEqualTo("2021-05-27");
    assertThat(result.getValueSetValues()).hasSize(72);

    // assert at least one value
    ValueSetObject actual = result.getValueSetValues().get("1468");
    assertThat(actual.getDisplay()).isEqualTo("ACON Laboratories, Inc, Flowflex SARS-CoV-2 Antigen rapid test");
    assertThat(actual.getLang()).isEqualTo("en");
    assertThat(actual.isActive()).isTrue();
    assertThat(actual.getVersion()).isEqualTo("2021-05-10 20:07:30 CET");
    assertThat(actual.getSystem()).isEqualTo("https://covid-19-diagnostics.jrc.ec.europa.eu/devices");
  }

  @Test
  void shouldReadDefaultTestResultIfNotConfigured() throws UnableToLoadFileException {
    var result = dgcToProtobufMapping.readTestResultJson();

    assertThat(result.getValueSetId()).isEqualTo(TEST_RESULT_ID);
    assertThat(result.getValueSetDate()).isEqualTo("2021-04-27");
    assertThat(result.getValueSetValues()).hasSize(2);

    // assert at least one value
    ValueSetObject actual = result.getValueSetValues().get("260373001");
    assertThat(actual.getDisplay()).isEqualTo("Detected");
    assertThat(actual.getLang()).isEqualTo("en");
    assertThat(actual.isActive()).isTrue();
    assertThat(actual.getVersion()).isEqualTo("http://snomed.info/sct/900000000000207008/version/20210131");
    assertThat(actual.getSystem()).isEqualTo("http://snomed.info/sct");
  }

  @Test
  void shouldReadDefaultTestTypeIfNotConfigured() throws UnableToLoadFileException {
    var result = dgcToProtobufMapping.readTestTypeJson();

    assertThat(result.getValueSetId()).isEqualTo(TEST_TYPE_ID);
    assertThat(result.getValueSetDate()).isEqualTo("2021-04-27");
    assertThat(result.getValueSetValues()).hasSize(2);

    // assert at least one value
    ValueSetObject actual = result.getValueSetValues().get("LP6464-4");
    assertThat(actual.getDisplay()).isEqualTo("Nucleic acid amplification with probe detection");
    assertThat(actual.getLang()).isEqualTo("en");
    assertThat(actual.isActive()).isTrue();
    assertThat(actual.getVersion()).isEqualTo("2.69");
    assertThat(actual.getSystem()).isEqualTo("http://loinc.org");
  }

  @Test
  void shouldReadDefaultDiseaseAgentTargetedIfNotConfigured() throws UnableToLoadFileException {
    var result = dgcToProtobufMapping.readDiseaseAgentTargetedJson();

    assertThat(result.getValueSetId()).isEqualTo(DISEASE_AGENT_TARGETED_ID);
    assertThat(result.getValueSetDate()).isEqualTo("2021-04-27");
    assertThat(result.getValueSetValues()).hasSize(1);

    // assert at least one value
    ValueSetObject actual = result.getValueSetValues().get("840539006");
    assertThat(actual.getDisplay()).isEqualTo("COVID-19");
    assertThat(actual.getLang()).isEqualTo("en");
    assertThat(actual.isActive()).isTrue();
    assertThat(actual.getVersion()).isEqualTo("http://snomed.info/sct/900000000000207008/version/20210131");
    assertThat(actual.getSystem()).isEqualTo("http://snomed.info/sct");
  }


}
