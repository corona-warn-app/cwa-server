package app.coronawarn.server.services.submission.audit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.web.context.annotation.RequestScope;

@RequestScope
public class TrackExecutionTimeProcessor {

  private Logger logger = LoggerFactory.getLogger(TrackExecutionTimeProcessor.class);
  public static final Marker TIMEOUT = MarkerFactory.getMarker("Timeout");

  private Map<String, Long> executionTimes = new LinkedHashMap<>();

  /**
   * Logs the time for the method executed.
   */
  public void logExecutionTimes() {
    for (Map.Entry<String, Long> entry : executionTimes.entrySet()) {
      logger.info(TIMEOUT, entry.getKey()
          + "() took " + entry.getValue() + " milliseconds.");
    }
  }

  /**
   * Add execution times for methods.
   *
   * @param name of the method that will be tracked
   * @param trackedTime execution time in millis for called method
   */
  public void addExecutionTime(String name, Long trackedTime) {
    executionTimes.put(name, trackedTime);
  }
}
