package app.coronawarn.server.common;

import app.coronawarn.server.common.persistence.service.common.LogMessages;

/**
 * Delegate logger that only accepts and correctly handles {@link LogMessages}.
 */
public class Logger {

  private final org.slf4j.Logger delegate;

  public Logger(org.slf4j.Logger logger) {
    this.delegate = logger;
  }

  /**
   * Log a message at the INFO level according to the specified format and
   * argument.
   * <p/>
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for
   * the INFO level.
   * </p>
   *
   * @param logMessage the {@link LogMessages} to log
   * @param arg        the argument
   */
  public void info(LogMessages logMessage, Object arg) {
    delegate.info(logMessage.toString(), arg);
  }

  /**
   * Log a message at the INFO level according to the specified format and
   * arguments.
   * <p/>
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for
   * the INFO level.
   * </p>
   *
   * @param logMessage the {@link LogMessages} to log
   * @param arg1       the first argument
   * @param arg2       the second argument
   */
  public void info(LogMessages logMessage, Object arg1, Object arg2) {
    delegate.info(logMessage.toString(), arg1, arg2);
  }

  /**
   * Log a message at the INFO level according to the specified format and
   * arguments.
   * <p/>
   * <p>
   * This form avoids superfluous string concatenation when the logger is disabled
   * for the INFO level. However, this variant incurs the hidden (and relatively
   * small) cost of creating an <code>Object[]</code> before invoking the method,
   * even if this logger is disabled for INFO. The variants taking
   * {@link #info(String, Object) one} and {@link #info(String, Object, Object)
   * two} arguments exist solely in order to avoid this hidden cost.
   * </p>
   *
   * @param logMessage the {@link LogMessages} to log
   * @param arguments  a list of 3 or more arguments
   */
  public void info(LogMessages logMessage, Object... arguments) {
    delegate.info(logMessage.toString(), arguments);
  }

  /**
   * Log a message at the WARN level according to the specified format and
   * argument.
   * <p/>
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for
   * the WARN level.
   * </p>
   *
   * @param logMessage The @see(LogMessage
   * @param arg        the argument
   */
  public void warn(LogMessages logMessage, Object arg) {
    delegate.warn(logMessage.toString(), arg);
  }

  /**
   * Log a message at the WARN level according to the specified format and
   * arguments.
   * <p/>
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for
   * the WARN level.
   * </p>
   *
   * @param logMessage the {@link LogMessages} to log
   * @param arg1       the first argument
   * @param arg2       the second argument
   */
  public void warn(LogMessages logMessage, Object arg1, Object arg2) {
    delegate.warn(logMessage.toString(), arg1, arg2);
  }

  /**
   * Log a message at the WARN level according to the specified format and
   * arguments.
   * <p/>
   * <p>
   * This form avoids superfluous string concatenation when the logger is disabled
   * for the WARN level. However, this variant incurs the hidden (and relatively
   * small) cost of creating an <code>Object[]</code> before invoking the method,
   * even if this logger is disabled for WARN. The variants taking
   * {@link #warn(String, Object) one} and {@link #warn(String, Object, Object)
   * two} arguments exist solely in order to avoid this hidden cost.
   * </p>
   *
   * @param logMessage the {@link LogMessages} to log
   * @param arguments  a list of 3 or more arguments
   */
  public void warn(LogMessages logMessage, Object... arguments) {
    delegate.warn(logMessage.toString(), arguments);
  }

  /**
   * Log a message at the ERROR level according to the specified format and
   * argument.
   * <p/>
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for
   * the ERROR level.
   * </p>
   *
   * @param logMessage the {@link LogMessages} to log
   * @param arg        the argument
   */
  public void error(LogMessages logMessage, Object arg) {
    delegate.error(logMessage.toString(), arg);
  }

  /**
   * Log a message at the ERROR level according to the specified format and
   * arguments.
   * <p/>
   * <p>
   * This form avoids superfluous object creation when the logger is disabled for
   * the ERROR level.
   * </p>
   *
   * @param logMessage the {@link LogMessages} to log
   * @param arg1       the first argument
   * @param arg2       the second argument
   */
  public void error(LogMessages logMessage, Object arg1, Object arg2) {
    delegate.error(logMessage.toString(), arg1, arg2);
  }

  /**
   * Log a message at the ERROR level according to the specified format and
   * arguments.
   * <p/>
   * <p>
   * This form avoids superfluous string concatenation when the logger is disabled
   * for the ERROR level. However, this variant incurs the hidden (and relatively
   * small) cost of creating an <code>Object[]</code> before invoking the method,
   * even if this logger is disabled for ERROR. The variants taking
   * {@link #error(String, Object) one} and {@link #error(String, Object, Object)
   * two} arguments exist solely in order to avoid this hidden cost.
   * </p>
   *
   * @param logMessage the {@link LogMessages} to log
   * @param arguments  a list of 3 or more arguments
   */
  public void error(LogMessages logMessage, Object... arguments) {
    delegate.error(logMessage.toString(), arguments);
  }

  /**
   * Log an exception (throwable) at the ERROR level with an accompanying message.
   *
   * @param logMessage the message accompanying the exception
   * @param t          the exception (throwable) to log
   */
  public void error(LogMessages logMessage, Throwable t) {
    delegate.error(logMessage.toString(), t);
  }
}
