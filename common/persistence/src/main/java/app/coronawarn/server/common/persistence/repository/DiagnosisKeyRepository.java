/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.common.persistence.repository;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosisKeyRepository extends PagingAndSortingRepository<DiagnosisKey, Long> {

  /**
   * Counts all entries that have a submission timestamp less or equal than the specified one.
   *
   * @param submissionTimestamp The submission timestamp up to which entries will be expired.
   * @return The number of expired keys.
   */
  @Query("SELECT COUNT(*) FROM diagnosis_key WHERE submission_timestamp<=:threshold")
  int countOlderThanOrEqual(@Param("threshold") long submissionTimestamp);

  /**
   * Counts all entries that have a submission timestamp less or equal than the specified one
   *  and match the given country_code.
   *
   * @param submissionTimestamp The submission timestamp up to which entries will be expired.
   * @return The number of expired keys.
   */
  @Query("SELECT COUNT(*) FROM diagnosis_key WHERE submission_timestamp<=:threshold AND "
      + ":country_code = ANY(visited_countries)")
  int countOlderThanOrEqual(@Param("threshold") long submissionTimestamp, @Param("country_code") String countryCode);

  /**
   * Returns all diagnosis keys where visited_countries list contains {@param countryCode} ordered by
   * submission_timestamp.
   *
   * @param countryCode filter country code.
   * @return list of DiagnosisKeys.
   */
  @Query("SELECT * FROM diagnosis_key WHERE :country_code = ANY(visited_countries) ORDER BY submission_timestamp ASC")
  Iterable<DiagnosisKey> findAllKeysWhereVisitedCountryContains(@Param("country_code") String countryCode);

  /**
   * Deletes all entries that have a submission timestamp less or equal than the specified one.
   *
   * @param submissionTimestamp The submission timestamp up to which entries will be deleted.
   */
  @Modifying
  @Query("DELETE FROM diagnosis_key WHERE submission_timestamp<=:threshold")
  void deleteOlderThanOrEqual(@Param("threshold") long submissionTimestamp);

  /**
   * Deletes all entries that have a submission timestamp less or equal than the specified one
   *  and match the origin country_code.
   *
   * @param submissionTimestamp The submission timestamp up to which entries will be deleted.
   */
  @Modifying
  @Query("DELETE FROM diagnosis_key WHERE submission_timestamp<=:threshold AND :country_code = ANY(visited_countries)")
  void deleteOlderThanOrEqual(@Param("threshold") long submissionTimestamp, @Param("country_code") String countryCode);

  /**
   * Attempts to write the specified diagnosis key information into the database. If a row with the specified key data
   * already exists, no data is inserted.
   *  @param keyData                    The key data of the diagnosis key.
   * @param rollingStartIntervalNumber The rolling start interval number of the diagnosis key.
   * @param rollingPeriod              The rolling period of the diagnosis key.
   * @param submissionTimestamp        The submission timestamp of the diagnosis key.
   * @param transmissionRisk           The transmission risk level of the diagnosis key.
   * @param originCountry              The origin country from the app.
   * @param visitedCountries           The list of countries this transmissions is relevant for.
   * @param verificationType           The verification type of the diagnosis key.
   */
  @Modifying
  @Query("INSERT INTO diagnosis_key "
      + "(key_data, rolling_start_interval_number, rolling_period, submission_timestamp, transmission_risk_level, "
        + "origin_country, visited_countries, verification_type) "
      + "VALUES (:keyData, :rollingStartIntervalNumber, :rollingPeriod, :submissionTimestamp, :transmissionRisk, "
        + ":origin_country, :visited_countries, :verificationType) "
      + "ON CONFLICT DO NOTHING")
  void saveDoNothingOnConflict(
      @Param("keyData") byte[] keyData,
      @Param("rollingStartIntervalNumber") int rollingStartIntervalNumber,
      @Param("rollingPeriod") int rollingPeriod,
      @Param("submissionTimestamp") long submissionTimestamp,
      @Param("transmissionRisk") int transmissionRisk,
      @Param("origin_country") String originCountry,
      @Param("visited_countries") String[] visitedCountries,
      @Param("verificationType") String verificationType);
}
