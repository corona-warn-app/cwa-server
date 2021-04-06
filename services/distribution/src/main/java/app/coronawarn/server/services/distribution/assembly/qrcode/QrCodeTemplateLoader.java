package app.coronawarn.server.services.distribution.assembly.qrcode;

import com.google.protobuf.ByteString;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class QrCodeTemplateLoader {

  private static final Logger logger = LoggerFactory.getLogger(QrCodeTemplateLoader.class);

  public ByteString loadAndroidTemplateAsBytes() {
    return loadPosterTemplate("pt-android-poster-1.0.0.xml");
  }

  public ByteString loadIosTemplateAsBytes() {
    return loadPosterTemplate("pt-ios-poster-1.0.0.pdf");
  }

  private ByteString loadPosterTemplate(String filename) {
    InputStream resourceAsStream = null;
    try {
      resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(filename);
      ByteString byteString = ByteString.readFrom(resourceAsStream);
      resourceAsStream.close();
      return byteString;
    } catch (IOException e) {
      logger.error(
          "Could not load '" + filename + "' QR poster template from the application package", e);
      //At the moment just log the error but do not interfere with normal application startup
      return ByteString.copyFromUtf8("");
    }
  }
}
