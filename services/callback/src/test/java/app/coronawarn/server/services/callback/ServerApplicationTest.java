package app.coronawarn.server.services.callback;

import static app.coronawarn.server.services.callback.ServerApplication.DISABLE_SSL_CLIENT_POSTGRES;
import static app.coronawarn.server.services.callback.ServerApplication.DISABLE_SSL_CLIENT_VERIFICATION;
import static app.coronawarn.server.services.callback.ServerApplication.DISABLE_SSL_CLIENT_VERIFICATION_VERIFY_HOSTNAME;
import static app.coronawarn.server.services.callback.ServerApplication.DISABLE_SSL_SERVER;
import static org.apache.logging.log4j.core.LoggerContext.getContext;
import static org.apache.logging.log4j.Level.DEBUG;
import static org.assertj.core.api.Assertions.assertThat;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class ServerApplicationTest {

  static List<String> messages = new ArrayList<>();

  private static final String LOGGER_NAME = ServerApplication.class.getName();
  private static final String APPENDER_NAME = TestAppender.class.getSimpleName();

  static class TestAppender implements Appender {

    @Override
    public State getState() {
      return State.STARTED;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isStarted() {
      return true;
    }

    @Override
    public boolean isStopped() {
      return false;
    }

    @Override
    public void append(LogEvent event) {
      String loggedMsg = event.getLevel() + " " + event.getMessage().getFormattedMessage();
      if (event.getThrown() != null) {
        loggedMsg += " " + event.getThrown();
      }
      messages.add(loggedMsg);
    }

    @Override
    public String getName() {
      return APPENDER_NAME;
    }

    @Override
    public Layout<? extends Serializable> getLayout() {
      return null;
    }

    @Override
    public boolean ignoreExceptions() {
      return true;
    }

    @Override
    public ErrorHandler getHandler() {
      return null;
    }

    @Override
    public void setHandler(ErrorHandler handler) {
    }
  }

  @BeforeAll
  protected static void beforeAll() {
    final Configuration config = getContext(false).getConfiguration();
    config.addLogger(LOGGER_NAME, new LoggerConfig(LOGGER_NAME, DEBUG, true));
    config.getRootLogger().addAppender(new TestAppender(), DEBUG, null);
  }

  @BeforeEach
  protected void beforeEach() {
    messages.clear();
  }

  @AfterAll
  protected static void afterAll() {
    final Configuration config = getContext(false).getConfiguration();
    config.removeLogger(LOGGER_NAME);
    config.getRootLogger().removeAppender(APPENDER_NAME);
  }

  @Test
  void testEndpointTlsWarningGetsLogged() {
    ServerApplication app = new ServerApplication();
    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles(DISABLE_SSL_SERVER);
    app.setEnvironment(env);

    assertThat(messages.get(1)).contains("endpoint TLS disabled");
  }

  @Test
  void testDBConnectionTlsWarningGetsLogged() {
    ServerApplication app = new ServerApplication();
    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles(DISABLE_SSL_CLIENT_POSTGRES);
    app.setEnvironment(env);

    assertThat(messages.get(1)).contains("postgres connection TLS disabled");
  }

  @Test
  void testVerificationTlsWarningGetsLogged() {
    ServerApplication app = new ServerApplication();
    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles(DISABLE_SSL_CLIENT_VERIFICATION);
    app.setEnvironment(env);

    assertThat(messages.get(1)).contains("verification service connection TLS disabled");
  }

  @Test
  void testHostnameTlsWarningGetsLogged() {
    ServerApplication app = new ServerApplication();
    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles(DISABLE_SSL_CLIENT_VERIFICATION_VERIFY_HOSTNAME);
    app.setEnvironment(env);

    assertThat(messages.get(1)).contains("verification service TLS hostname validation disabled");
  }
}
