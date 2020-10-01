

package app.coronawarn.server.services.submission.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FakeDelayManagerTest {

  private static final long VALID_DELAY = 1000L;
  private static final long VALID_SAMPLE_SIZE = 1L;
  private final SubmissionServiceConfig config = mock(SubmissionServiceConfig.class);

  @BeforeEach
  void setup() {
    reset(config);
  }

  @Test
  void jitteredFakeDelayGreaterThanOrEqualZero() {
    FakeDelayManager fakeDelayManager = buildFakeDelayManager(1L, 1L);
    long actFakeDelay = fakeDelayManager.getJitteredFakeDelay();
    assertThat(actFakeDelay).isGreaterThanOrEqualTo(0L);
  }

  @Test
  void testUpdateFakeDelayForSampleSizeOne() {
    FakeDelayManager fakeDelayManager = buildFakeDelayManager(1000L, 1L);
    fakeDelayManager.updateFakeRequestDelay(2000L);
    assertThat(fakeDelayManager.getFakeDelayInSeconds()).isEqualTo(2d);
  }

  @Test
  void testUpdateFakeDelayForSampleSizeTwo() {
    FakeDelayManager fakeDelayManager = buildFakeDelayManager(1000L, 2L);
    fakeDelayManager.updateFakeRequestDelay(3000L);
    assertThat(fakeDelayManager.getFakeDelayInSeconds()).isEqualTo(2d);
  }

  @Test
  void testUpdateFakeDelayForConstantRequestTime() {
    FakeDelayManager fakeDelayManager = buildFakeDelayManager(1000L, 3L);
    fakeDelayManager.updateFakeRequestDelay(1000L);
    fakeDelayManager.updateFakeRequestDelay(1000L);
    assertThat(fakeDelayManager.getFakeDelayInSeconds()).isEqualTo(1d);
  }

  @Test
  void testGetFakeDelayInSecondsForInitialDelay() {
    FakeDelayManager fakeDelayManager = buildFakeDelayManager(VALID_DELAY, VALID_SAMPLE_SIZE);
    assertThat(fakeDelayManager.getFakeDelayInSeconds()).isEqualTo(VALID_DELAY / 1000d);
  }

  private FakeDelayManager buildFakeDelayManager(long initialDelay, long movingAverageSampleSize) {
    when(config.getInitialFakeDelayMilliseconds()).thenReturn(initialDelay);
    when(config.getFakeDelayMovingAverageSamples()).thenReturn(movingAverageSampleSize);
    return new FakeDelayManager(config);
  }
}
