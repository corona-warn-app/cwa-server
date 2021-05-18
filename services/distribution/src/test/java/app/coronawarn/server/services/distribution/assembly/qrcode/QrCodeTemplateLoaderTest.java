package app.coronawarn.server.services.distribution.assembly.qrcode;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import com.google.protobuf.ByteString;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.core.io.ResourceLoader;
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

  @Autowired
  private QrCodeTemplateLoader loader;

  @Test
  public void testLoadPosterTemplateCanLoadDefaultOnError() throws IOException {
    config.getAndroidQrCodePosterTemplate().setTemplate("non/existent.file");
    ByteString template = loader.loadAndroidTemplateAsBytes();

    assertThat(template).isNotEmpty();
  }

  @Test
  public void testLoadPosterTemplateCanLoadDefaultMissingFile() throws IOException {
    config.getIosQrCodePosterTemplate().setTemplate(null);
    ByteString template = loader.loadIosTemplateAsBytes();

    assertThat(template).isNotEmpty();
  }

  @Test
  public void testLoadPosterTemplateCanLoadFileFromDisk() throws IOException {
    String uri = new File("src/main/resources/pt-android-poster-1.0.0.pdf").getAbsoluteFile().toURI().toString();
    ByteString template = loader.loadPosterTemplate(uri, "non-existent");

    assertThat(template).isNotEmpty();
  }

  @Test
  public void testWrongFallbackDoesNotThrowException() throws IOException {
    ByteString template = loader.loadPosterTemplate("non-existent", "non-existent");

    assertThat(template).isEmpty();
  }
}
