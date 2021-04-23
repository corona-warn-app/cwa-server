package app.coronawarn.server.services.distribution.assembly.qrcode;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import com.google.protobuf.ByteString;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class QrCodeTemplateLoader {

  private static final Logger logger = LoggerFactory.getLogger(QrCodeTemplateLoader.class);

  private final DistributionServiceConfig config;

  public QrCodeTemplateLoader(DistributionServiceConfig config) {
    this.config = config;
  }

  public ByteString loadAndroidTemplateAsBytes() {
    return loadPosterTemplate(config.getAndroidQrCodePosterTemplate().getTemplate(), "pt-android-poster-1.0.0.pdf");
  }

  public ByteString loadIosTemplateAsBytes() {
    return loadPosterTemplate(config.getIosQrCodePosterTemplate().getTemplate(), "pt-ios-poster-1.0.0.pdf");
  }

  private ByteString loadPosterTemplate(String filename, String fallback) {
    try (InputStream fileStream = new FileInputStream(filename)) {
      return ByteString.readFrom(fileStream);
    } catch (IOException e) {
      logger.error(
          "Could not load '" + filename + "' QR poster template, will load default from the application package: {} - {}",
          e.getClass().getName(), e.getMessage());
      try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(fallback)) {
        return ByteString.readFrom(resourceAsStream);
      } catch (Exception e2) {
        logger.error(
            "Could not load default QR poster template from the application package!", e);
      }
    }
    // At the moment just log the error but do not interfere with normal application startup
    return ByteString.copyFromUtf8("");
  }
}
