package app.coronawarn.server.services.callback;

import static app.coronawarn.server.services.callback.ServerApplication.DISABLE_SSL_CLIENT_POSTGRES;
import static app.coronawarn.server.services.callback.ServerApplication.DISABLE_SSL_CLIENT_VERIFICATION;
import static app.coronawarn.server.services.callback.ServerApplication.DISABLE_SSL_CLIENT_VERIFICATION_VERIFY_HOSTNAME;
import static app.coronawarn.server.services.callback.ServerApplication.DISABLE_SSL_SERVER;
import static org.assertj.core.api.Assertions.assertThat;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class ServerApplicationTest {

  static List<String> messages = new ArrayList<>();

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
      return TestAppender.class.getSimpleName();
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
    final LoggerContext context = LoggerContext.getContext(false);
    final Configuration config = context.getConfiguration();
    TestAppender appender = new TestAppender();
    config.addLogger(ServerApplication.class.getName(),
        new LoggerConfig(ServerApplication.class.getName(), Level.ALL, true));
    config.getRootLogger().addAppender(appender, Level.ALL, null);
  }

  @BeforeEach
  protected void beforeEach() {
    messages.clear();
  }

  @AfterAll
  protected static void afterAll() {
    final LoggerContext context = LoggerContext.getContext(false);
    final Configuration config = context.getConfiguration();
    config.getRootLogger().removeAppender(ServerApplication.class.getName());
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
