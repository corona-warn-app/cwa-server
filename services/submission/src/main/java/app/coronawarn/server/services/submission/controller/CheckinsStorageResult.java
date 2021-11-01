package app.coronawarn.server.services.submission.controller;

/**
 * Simple data structure that holds information about checkins processing during submission.
 */
public class CheckinsStorageResult {

  private final int numberOfFilteredCheckins;
  private final int numberOfSavedCheckins;

  /**
   * Creates an instance.
   *
   * @param numberOfFilteredCheckins  Total number of checkins which were filtered
   * @param numberOfSavedCheckins     Total number of checkins stored in the db.
   */
  public CheckinsStorageResult(int numberOfFilteredCheckins, int numberOfSavedCheckins) {
    this.numberOfFilteredCheckins = numberOfFilteredCheckins;
    this.numberOfSavedCheckins = numberOfSavedCheckins;
  }

  public int getNumberOfFilteredCheckins() {
    return numberOfFilteredCheckins;
  }

  public int getNumberOfSavedCheckins() {
    return numberOfSavedCheckins;
  }
}
