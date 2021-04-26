

package app.coronawarn.server.services.submission.controller;

import app.coronawarn.server.services.submission.audit.TrackExecutionTime;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.springframework.stereotype.Component;

/**
 * {@link FakeDelayManager} instances manage the response delay in the processing of fake (or "dummy") requests.
 */
@Component
public class FakeDelayManager {

  private final long movingAverageSampleSize;
  private long fakeDelay;

  public FakeDelayManager(SubmissionServiceConfig submissionServiceConfig) {
    this.fakeDelay = submissionServiceConfig.getInitialFakeDelayMilliseconds();
    this.movingAverageSampleSize = submissionServiceConfig.getFakeDelayMovingAverageSamples();
  }

  /**
   * Returns the current fake delay after applying random jitter.
   *
   * @return the fake delay
   */
  @TrackExecutionTime
  public long getJitteredFakeDelay() {
    return new PoissonDistribution(fakeDelay).sample();
  }

  /**
   * Updates the moving average for the request duration with the specified value.
   *
   * @param realRequestDuration the request duration
   */
  public void updateFakeRequestDelay(long realRequestDuration) {
    final long currentDelay = fakeDelay;
    fakeDelay = currentDelay + (realRequestDuration - currentDelay) / movingAverageSampleSize;
  }

  /**
   * Returns the current fake delay in seconds. Used for monitoring.
   *
   * @return fake delay in seconds
   */
  public Double getFakeDelayInSeconds() {
    return fakeDelay / 1000.;
  }
}
