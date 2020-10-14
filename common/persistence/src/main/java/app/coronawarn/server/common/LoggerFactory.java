package app.coronawarn.server.common;

import org.slf4j.ILoggerFactory;

public class LoggerFactory {

  /**
   * Return a logger named corresponding to the class passed as parameter, using
   * the statically bound {@link ILoggerFactory} instance.
   * 
   * <p>In case the the <code>clazz</code> parameter differs from the name of the
   * caller as computed internally by SLF4J, a logger name mismatch warning will
   * be printed but only if the <code>slf4j.detectLoggerNameMismatch</code> system
   * property is set to true. By default, this property is not set and no warnings
   * will be printed even in case of a logger name mismatch.
   * 
   * @param clazz the returned logger will be named after clazz
   * @return logger
   * 
   * 
   * @see <a href="http://www.slf4j.org/codes.html#loggerNameMismatch">Detected
   *      logger name mismatch</a>
   */
  public static Logger getLogger(Class<?> clazz) {
    return new Logger(org.slf4j.LoggerFactory.getLogger(clazz));
  }

}
