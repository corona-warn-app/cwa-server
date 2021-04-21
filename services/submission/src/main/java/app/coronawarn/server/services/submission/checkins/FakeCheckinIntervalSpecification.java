package app.coronawarn.server.services.submission.checkins;

import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import java.security.SecureRandom;
import java.util.Random;
import java.util.function.Function;

public class FakeCheckinIntervalSpecification {

  private static final Random RANDOM = new SecureRandom();

  /**
   * https://github.com/corona-warn-app/cwa-app-tech-spec/blob/proposal/event-registration-mvp
   * /docs/spec/event-registration-server.md#generating-fake-tracetimeintervalwarnings
   */
  public static final Function<CheckIn, Integer> END_INTERVAL_GENERATION = (checkin) -> {
    int period = checkin.getEndIntervalNumber() - checkin.getStartIntervalNumber();
    if (period == 144) {
      return checkin.getEndIntervalNumber();
    } else {
      int sign = RANDOM.nextDouble() > 0.5 ? 1 : -1;
      int offset = RANDOM.nextInt(3);
      return checkin.getStartIntervalNumber() + Math.max(2, period + sign * offset);
    }
  };

  /**
   * https://github.com/corona-warn-app/cwa-app-tech-spec/blob/proposal/event-registration-mvp
   * /docs/spec/event-registration-server.md#generating-fake-tracetimeintervalwarnings
   */
  public static final Function<CheckIn, Integer> START_INTERVAL_GENERATION = (checkin) -> {
    int startIntervalNumber = checkin.getStartIntervalNumber();
    int minStartIntervalNumber = (int) (Math.floor(startIntervalNumber / 144d) * 144);
    if (minStartIntervalNumber == startIntervalNumber) {
      return startIntervalNumber;
    } else {
      int sign = RANDOM.nextDouble() > 0.5d ? 1 : -1;
      int offset = RANDOM.nextInt(6);
      return Math.max(minStartIntervalNumber, startIntervalNumber + sign * offset);
    }
  };
}
