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

  private static String path1 = "src/test/resources/dgc/vaccine-mah-manf.json";

  @Test
  public void shouldRunParsing() throws IOException {
    JsonFileLoader jsonFileLoader = Mockito.mock(JsonFileLoader.class);
    JsonFile json1 = new JsonFile(new FileInputStream(path1), "");

    Mockito.when(jsonFileLoader.getFile()).thenReturn(json1);
    DigitalGreenCertificateToProtobufMapping dgcToProtobufMapping = new DigitalGreenCertificateToProtobufMapping(distributionServiceConfig, jsonFileLoader);

    var result = dgcToProtobufMapping.readMahManfJson();

    assertThat(result.getValueSetId()).isEqualTo("vaccines-covid-19-auth-holders");



    // 1. how to set the JsonReader to the file from DigitalGreenCertificateToProtobufMapping
    //  with the values comming vaccin

    // 2. check if values are read corectly

    // 3. how they are combined in the protobuf file


  }

}
