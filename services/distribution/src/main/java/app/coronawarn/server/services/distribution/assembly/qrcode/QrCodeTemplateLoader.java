package app.coronawarn.server.services.distribution.assembly.qrcode;

import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class QrCodeTemplateLoader {

  private static final Logger logger = LoggerFactory.getLogger(QrCodeTemplateLoader.class);

  @Autowired
  DistributionServiceConfig config;

  @Autowired
  ResourceLoader resourceLoader;

  public ByteString loadAndroidTemplateAsBytes() {
    return loadPosterTemplate(config.getAndroidQrCodePosterTemplate().getTemplate(),
        CLASSPATH_URL_PREFIX + "pt-android-poster-1.0.0.pdf");
  }

  public ByteString loadIosTemplateAsBytes() {
    return loadPosterTemplate(config.getIosQrCodePosterTemplate().getTemplate(),
        CLASSPATH_URL_PREFIX + "pt-ios-poster-1.0.0.pdf");
  }

  private ByteString loadPosterTemplate(String filename, String fallback) {
    try (InputStream fileStream = resourceLoader.getResource(filename).getInputStream()) {
      return ByteString.readFrom(fileStream);
    } catch (IOException e) {
      logger.error(
          "Error loading QR poster template from '" + filename + "', loading default from application package: {} ({})",
          e.getClass().getName(), e.getMessage());
      try (InputStream resourceAsStream = resourceLoader.getResource(fallback).getInputStream()) {
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
