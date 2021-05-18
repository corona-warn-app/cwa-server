package app.coronawarn.server.services.distribution.assembly.qrcode;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class QrCodeTemplateLoader {

  private static final Logger logger = LoggerFactory.getLogger(QrCodeTemplateLoader.class);

  @Autowired
  DistributionServiceConfig config;

  @Autowired
  ResourceLoader resourceLoader;

  public ByteString loadAndroidTemplateAsBytes() {
    return loadPosterTemplate(config.getAndroidQrCodePosterTemplate().getTemplate(), "pt-android-poster-1.0.0.pdf");
  }

  public ByteString loadIosTemplateAsBytes() {
    return loadPosterTemplate(config.getIosQrCodePosterTemplate().getTemplate(), "pt-ios-poster-1.0.0.pdf");
  }

  protected ByteString loadPosterTemplate(String filename, String fallback) {
    if (!ObjectUtils.isEmpty(filename)) {
      try (InputStream fileStream = resourceLoader.getResource(filename).getInputStream()) {
        logger.debug("Loading QR poster template from {}.", filename);
        return ByteString.readFrom(fileStream);
      } catch (IOException e) {
        logger.error("Error loading QR poster template from '{}'.", filename, e);
      }
    }
    try (InputStream resourceAsStream = resourceLoader.getResource(fallback).getInputStream()) {
      // fallback to default
      logger.debug("QR poster template to load was empty or invalid, falling back to loading from {}.", fallback);
      return ByteString.readFrom(resourceAsStream);
    } catch (Exception e) {
      logger.error("Could not load default QR poster template {}. This shouldn't happen!", fallback, e);
    }
    // At the moment just log the error but do not interfere with normal application startup
    return ByteString.copyFromUtf8("");
  }
}
