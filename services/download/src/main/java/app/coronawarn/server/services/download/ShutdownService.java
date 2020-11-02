package app.coronawarn.server.services.download;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Service class for wrapping system exit calls.
 */
@Component
public class ShutdownService {

  /**
   * Shutdown the application.
   */
  public void shutdownApplication(ApplicationContext applicationContext) {
    Application.killApplication(applicationContext);
  }
}
