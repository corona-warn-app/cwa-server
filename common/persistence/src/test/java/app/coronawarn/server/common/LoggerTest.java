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

  LogMessages msg1Arg = new LogMessages() {
    @Override
    public String toString() {
      return "This is a {}.";
    }
  };

  LogMessages msg2Args = new LogMessages() {
    @Override
    public String toString() {
      return "This is a {} with a {}.";
    }
  };

  LogMessages msg3Args = new LogMessages() {
    @Override
    public String toString() {
      return "This is a {} with a {} and a {}.";
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
  void testLoggingDebugWith1ArgLogsDebug() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.debug(msg1Arg, "test");
    assertThat(messages.get(0)).isEqualTo("DEBUG This is a test.");
  }

  @Test
  void testLoggingDebugWith2ArgsLogsDebug() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.debug(msg2Args, "test", "info");
    assertThat(messages.get(0)).isEqualTo("DEBUG This is a test with a info.");
  }

  @Test
  void testLoggingDebugWith3ArgsLogsDebug() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.debug(msg3Args, "test", "info", "now you know");
    assertThat(messages.get(0)).isEqualTo("DEBUG This is a test with a info and a now you know.");
  }

  @Test
  void testLoggingInfoWith1ArgLogsInfo() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.info(msg1Arg, "test");
    assertThat(messages.get(0)).isEqualTo("INFO This is a test.");
  }

  @Test
  void testLoggingInfoWith2ArgsLogsInfo() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.info(msg2Args, "test", "info");
    assertThat(messages.get(0)).isEqualTo("INFO This is a test with a info.");
  }

  @Test
  void testLoggingInfoWith3ArgsLogsInfo() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.info(msg3Args, "test", "info", "now you know");
    assertThat(messages.get(0)).isEqualTo("INFO This is a test with a info and a now you know.");
  }

  @Test
  void testLoggingWarnWith1ArgLogsWarning() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.warn(msg1Arg, "warning");
    assertThat(messages.get(0)).isEqualTo("WARN This is a warning.");
  }

  @Test
  void testLoggingWarnWith2ArgLogsWarning() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.warn(msg2Args, "warning", "problem");
    assertThat(messages.get(0)).isEqualTo("WARN This is a warning with a problem.");
  }

  @Test
  void testLoggingWarnWith3ArgLogsWarning() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.warn(msg3Args, "warning", "problem", "you should be more careful");
    assertThat(messages.get(0)).isEqualTo("WARN This is a warning with a problem and a you should be more careful.");
  }

  @Test
  void testLoggingErrorWith1ArgLogsError() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.error(msg1Arg, "failure");
    assertThat(messages.get(0)).isEqualTo("ERROR This is a failure.");
  }

  @Test
  void testLoggingErrorWith2ArgsLogsError() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.error(msg2Args, "mistake", "failure");
    assertThat(messages.get(0)).isEqualTo("ERROR This is a mistake with a failure.");
  }

  @Test
  void testLoggingErrorWith3ArgsLogsError() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.error(msg3Args, "mistake", "failure", "you are dumb");
    assertThat(messages.get(0)).isEqualTo("ERROR This is a mistake with a failure and a you are dumb.");
  }

  @Test
  void testExceptionIsLoggedWithArg() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.error(msg1Arg, "failure", new IllegalStateException());
    assertThat(messages.get(0)).isEqualTo("ERROR This is a failure. java.lang.IllegalStateException");
  }

  @Test
  void testExceptionIsLogged() {
    Logger logger = LoggerFactory.getLogger(getClass());
    logger.error(msg1Arg, new IllegalStateException());
    assertThat(messages.get(0)).isEqualTo("ERROR This is a {}. java.lang.IllegalStateException");
  }
}
