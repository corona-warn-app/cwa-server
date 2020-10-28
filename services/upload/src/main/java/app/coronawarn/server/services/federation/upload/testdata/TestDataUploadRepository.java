

package app.coronawarn.server.services.federation.upload.testdata;

import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@Profile("testdata")
public interface TestDataUploadRepository
    extends org.springframework.data.repository.Repository<FederationUploadKey, Long> {

  @Modifying
  @Query("INSERT INTO federation_upload_key "
      + "(key_data, rolling_start_interval_number, rolling_period, submission_timestamp, transmission_risk_level, "
      + "origin_country, visited_countries, report_type, days_since_onset_of_symptoms, consent_to_federation) "
      + "VALUES (:keyData, :rollingStartIntervalNumber, :rollingPeriod, :submissionTimestamp, :transmissionRisk, "
      + ":origin_country, :visited_countries, :report_type, :days_since_onset_of_symptoms, :consent_to_federation) "
      + "ON CONFLICT DO NOTHING")
  void storeUploadKey(
      @Param("keyData") byte[] keyData,
      @Param("rollingStartIntervalNumber") int rollingStartIntervalNumber,
      @Param("rollingPeriod") int rollingPeriod,
      @Param("submissionTimestamp") long submissionTimestamp,
      @Param("transmissionRisk") int transmissionRisk,
      @Param("origin_country") String originCountry,
      @Param("visited_countries") String[] visitedCountries,
      @Param("report_type") String reportType,
      @Param("days_since_onset_of_symptoms") int daysSinceOnsetOfSymptoms,
      @Param("consent_to_federation") boolean consentToFederation);

  @Query("SELECT COUNT(*) FROM federation_upload_key WHERE batch_tag IS NULL")
  Integer countPendingKeys();

  @Modifying
  @Query("DELETE FROM federation_upload_key WHERE rolling_start_interval_number<=:retention")
  void applyRetentionToTestKeys(@Param("retention") int retention);
}
