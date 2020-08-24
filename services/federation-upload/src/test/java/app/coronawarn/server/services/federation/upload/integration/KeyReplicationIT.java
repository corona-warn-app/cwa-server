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

package app.coronawarn.server.services.federation.upload.integration;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.repository.FederationUploadKeyRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.federation.upload.Application;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Application.class}, initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles({"integration-test"})
public class KeyReplicationIT {

    @Autowired
    private DiagnosisKeyService keyService;

    @Autowired
    private FederationUploadKeyRepository uploadKeyRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUpMocks() {
      jdbcTemplate.execute("truncate table federation_upload_key");
    }

    @Test
    void diagnosisKeysWithConsentShouldBeReplicatedOnInsert() {
       DiagnosisKey dummyKey = DiagnosisKey.builder().withKeyData(randomByteData())
                              .withRollingStartIntervalNumber(1)
                              .withTransmissionRiskLevel(2)
                              .withConsentToFederation(true)
                              .withCountryCode("DE")
                              .withDaysSinceOnsetOfSymptoms(1)
                              .withSubmissionTimestamp(12)
                              .withVisitedCountries(List.of("FR","DK"))
                              .withReportType(ReportType.CONFIRMED_TEST)
                              .build();
       keyService.saveDiagnosisKeys(List.of(dummyKey));

       Collection<DiagnosisKey> uploadableKeys = uploadKeyRepository.findAllUploadableKeys();

       assertTrue(uploadableKeys.size() == 1);
       assertTrue(uploadableKeys.iterator().next().equals(dummyKey));
    }

    private byte[] randomByteData() {
      byte[] keydata = new byte[16];
      new Random().nextBytes(keydata);
      return keydata;
    }
}
