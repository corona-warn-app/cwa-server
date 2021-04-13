package app.coronawarn.server.services.submission.checkins;

@SuppressWarnings("serial")
public class TooManyCheckInsAtSameDay extends RuntimeException {

  public TooManyCheckInsAtSameDay(final int startIntervalNumber) {
    super("Too many check-ins with startIntervalNumber = " + startIntervalNumber);
  }
}
