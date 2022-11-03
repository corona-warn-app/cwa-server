package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosisKeyRepository extends PagingAndSortingRepository<DiagnosisKey, Long> {

  /**
   * Returns whether or not a diagnosis key with the specified key data and submission type exists in the DB.
   *
   * @param keyData        The key data to search for
   * @param submissionType The submission type to search for
   * @return whether or not a diagnosis key with the specified key data and submission type exists in the DB
   */
  @Query("SELECT CAST(CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END AS BIT) "
      + "FROM diagnosis_key "
      + "WHERE key_data=:key_data "
      + "AND submission_type=:submission_type")
  boolean exists(@Param("key_data") byte[] keyData, @Param("submission_type") String submissionType);

  /**
   * Counts all entries that have a submission timestamp older than the specified one.
   *
   * @param submissionTimestamp The submission timestamp up to which entries will be expired.
   * @return The number of expired keys.
   */
  @Query("SELECT COUNT(*) FROM diagnosis_key WHERE submission_timestamp<:threshold")
  int countOlderThan(@Param("threshold") long submissionTimestamp);

  /**
   * Deletes all entries that have a submission timestamp older than the specified one.
   *
   * @param submissionTimestamp The submission timestamp up to which entries will be deleted.
   */
  @Modifying
  @Query("DELETE FROM diagnosis_key WHERE submission_timestamp<:threshold")
  void deleteOlderThan(@Param("threshold") long submissionTimestamp);

  /**
   * Attempts to write the specified diagnosis key information into the database. If a row with the specified key data
   * already exists, no data is inserted.
   *
   * @param keyData                    The key data of the diagnosis key.
   * @param rollingStartIntervalNumber The rolling start interval number of the diagnosis key.
   * @param rollingPeriod              The rolling period of the diagnosis key.
   * @param submissionTimestamp        The submission timestamp of the diagnosis key.
   * @param transmissionRisk           The transmission risk level of the diagnosis key.
   * @param originCountry              The origin country from the app.
   * @param visitedCountries           The list of countries this transmissions is relevant for.
   * @param reportType                 The report type of the diagnosis key.
   * @param daysSinceOnsetOfSymptoms   The number of days since symptoms began.
   * @param consentToFederation        The consent to federation.
   * @param submissionType             The submission type
   */
  @Modifying
  @Query("INSERT INTO diagnosis_key "
      + "(key_data, rolling_start_interval_number, rolling_period, submission_timestamp, transmission_risk_level, "
      + "origin_country, visited_countries, report_type, days_since_onset_of_symptoms, consent_to_federation, "
      + "submission_type) "
      + "VALUES (:keyData, :rollingStartIntervalNumber, :rollingPeriod, :submissionTimestamp, :transmissionRisk, "
      + ":origin_country, :visited_countries, :report_type, :days_since_onset_of_symptoms, :consent_to_federation, "
      + ":submission_type) "
      + "ON CONFLICT DO NOTHING")
  boolean saveDoNothingOnConflict(
      @Param("keyData") byte[] keyData,
      @Param("rollingStartIntervalNumber") int rollingStartIntervalNumber,
      @Param("rollingPeriod") int rollingPeriod,
      @Param("submissionTimestamp") long submissionTimestamp,
      @Param("transmissionRisk") int transmissionRisk,
      @Param("origin_country") String originCountry,
      @Param("visited_countries") String[] visitedCountries,
      @Param("report_type") String reportType,
      @Param("days_since_onset_of_symptoms") int daysSinceOnsetOfSymptoms,
      @Param("consent_to_federation") boolean consentToFederation,
      @Param("submission_type") String submissionType);

  /**
   * <code>SELECT * FROM diagnosis_key WHERE transmission_risk_level>=:minTRL ORDER BY submission_timestamp</code>.
   *
   * @param minTrl minimum Transmission-Risk-Level to be fetched from the database
   * @return List of {@link DiagnosisKey}s with given TRL or higher
   */
  @Query("SELECT * FROM diagnosis_key WHERE transmission_risk_level>=:minTrl AND submission_timestamp>=:threshold "
      + " ORDER BY submission_timestamp")
  List<DiagnosisKey> findAllWithTrlGreaterThanOrEqual(final @Param("minTrl") int minTrl,
      @Param("threshold") long submissionTimestamp);

  /**
   * For each Self-Reported-Submission, we'll create one record to get a glimpse on what's going on.
   * 
   * @param submissionType - depending on what the client has chosen
   * @return <code>true</code>, when insert into self_report_submissions was successful
   */
  @Modifying
  @Query("INSERT INTO self_report_submissions "
      + "(submission_type) "
      + "VALUES (:submission_type) ")
  boolean recordSrs(final @Param("submission_type") String submissionType);
}
