package app.coronawarn.server.common;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoggerTest {

  List<String> messages = new ArrayList<>();

  LogMessages message = new LogMessages() {
    @Override
    public String toString() {
      return "This is a {}.";
    }
  };

  class TestAppender implements Appender {

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
      if (event.getThrown() != null){
        loggedMsg += " " + event.getThrown();
      }
      messages.add( loggedMsg );
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

  @BeforeEach
  protected void beforeEach() {
    messages.clear();
    final LoggerContext context = LoggerContext.getContext(false);
    final Configuration config = context.getConfiguration();
    TestAppender appender = new TestAppender();
    config.addLogger(getClass().getName(), new LoggerConfig(getClass().getName(), Level.ALL, true));
    config.getRootLogger().addAppender(appender, Level.ALL, null);
  }

  @AfterEach
  protected void afterEach() {
    final LoggerContext context = LoggerContext.getContext(false);
    final Configuration config = context.getConfiguration();
    config.getRootLogger().removeAppender(TestAppender.class.getSimpleName());
  }

  @Test
  void testLoggingInfoLogsInfo() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.info(message, "test");
    assertThat(messages.get(0)).isEqualTo("INFO This is a test.");
  }

  @Test
  void testLoggingWarnLogsWarning() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.warn(message, "warning");
    assertThat(messages.get(0)).isEqualTo("WARN This is a warning.");
  }

  @Test
  void testLoggingErrorLogsError() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.error(message, "failure");
    assertThat(messages.get(0)).isEqualTo("ERROR This is a failure.");
  }

  @Test
  void testExceptionIsLogged() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.error(message, "failure", new IllegalStateException());
    assertThat(messages.get(0)).isEqualTo("ERROR This is a failure. java.lang.IllegalStateException");
  }
}
