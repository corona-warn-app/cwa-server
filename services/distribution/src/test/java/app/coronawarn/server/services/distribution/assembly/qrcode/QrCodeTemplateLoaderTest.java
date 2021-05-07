package app.coronawarn.server.services.distribution.assembly.qrcode;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import com.google.protobuf.ByteString;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {QrCodeTemplateLoader.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class QrCodeTemplateLoaderTest {

  @Autowired
  private DistributionServiceConfig config;

  @Test
  public void testLoadPosterTemplateCanLoadDefaultOnError() throws IOException {
    config.getAndroidQrCodePosterTemplate().setTemplate("non/existent.file");
    QrCodeTemplateLoader loader = new QrCodeTemplateLoader(config);
    ByteString template = loader.loadAndroidTemplateAsBytes();

    assertThat(template).isNotEmpty();
  }

  @Test
  public void testLoadPosterTemplateCanLoadFileFromDisk() throws IOException {
    config.getAndroidQrCodePosterTemplate().setTemplate("src/main/resources/pt-android-poster-1.0.0.pdf");
    QrCodeTemplateLoader loader = new QrCodeTemplateLoader(config);
    ByteString template = loader.loadAndroidTemplateAsBytes();

    assertThat(template).isNotEmpty();
  }
}
