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
   * @param numberOfFilteredCheckins Total number of checkins which were filtered.
   * @param numberOfSavedCheckins    Total number of checkins stored in the db.
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

  /**
   * Creates a new updated CheckinsStorageResult that contains the summed values of both this & other.
   *
   * @param other CheckinsStorageResult with numberOfFilteredCheckins & numberOfSavedCheckins to be added.
   * @return updated CheckinsStorageResult.
   */
  public CheckinsStorageResult update(CheckinsStorageResult other) {
    return new CheckinsStorageResult(this.getNumberOfFilteredCheckins()
        + other.getNumberOfFilteredCheckins(), this.getNumberOfSavedCheckins()
        + other.getNumberOfSavedCheckins());
  }
}
