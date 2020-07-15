
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

package app.coronawarn.server.services.distribution.objectstore.integration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.distribution.Application;
import app.coronawarn.server.services.distribution.assembly.component.OutputDirectoryProvider;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.common.DiagnosisTestData;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.FailedObjectStoreOperationsCounter;
import app.coronawarn.server.services.distribution.objectstore.ObjectStoreAccess;
import app.coronawarn.server.services.distribution.objectstore.S3Publisher;
import app.coronawarn.server.services.distribution.objectstore.S3RetentionPolicy;
import app.coronawarn.server.services.distribution.objectstore.client.S3Object;
import app.coronawarn.server.services.distribution.runner.Assembly;
import app.coronawarn.server.services.distribution.runner.RetentionPolicy;
import app.coronawarn.server.services.distribution.runner.S3Distribution;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Application.class}, 
					  initializers = ConfigFileApplicationContextInitializer.class)
@Tag("s3-integration")
@ActiveProfiles("integration-test")
public class ObjectStoreFilePreservationIT {

	@Autowired
	private DiagnosisKeyService diagnosisKeyService;
	@Autowired
	private Assembly fileAssembler;
	@Autowired
	private S3Distribution s3Distribution;
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private  S3RetentionPolicy s3RetentionPolicy;
	@Autowired
	private  ObjectStoreAccess objectStoreAccess;
	@Autowired
	private DistributionServiceConfig distributionServiceConfig;
	
	@MockBean
	private OutputDirectoryProvider distributionDirectoryProvider;
	
	@Rule
	private TemporaryFolder testOutputFolder = new TemporaryFolder();
	  
	
	@BeforeEach
	public void setup() throws IOException {
		testOutputFolder.create();
		objectStoreAccess.deleteObjectsWithPrefix(distributionServiceConfig.getApi().getVersionPath());
	}
	
	/**
	 * 
	 *  The test covers a behaviour that manifests itself when data retention and shifting policies cause 
	 *  the file distribution logic to generate, in subsequent runs, different content for the same timeframes.
	 *  In order to test the distribution problem we need to simulate a simple daily scenario as test fixture:
	 *  
	 *  The test presumes there are 4 consecutive days with 80 keys submitted daily. Running a distribution in Day 4
	 *  would result in:
	 *  	Day 1   -> submission of 80 keys   -> 1 empty file distributed
	 *      Day 2   -> submission of 80 keys   -> 1 distributed containing 160 keys
	 *      Day 3   -> submission of 80 keys   -> 1 empty file distributed
	 *      Day 4   -> submission of 80 keys   -> 1 distributed containing 160 keys
	 *      
	 *  All these files should not be changed/removed from S3 even after retention policies have been applied. 
	 *  For example, if data in Day 1 gets removed completely, then a second 
	 *  distribution run causes a shifting of keys in different files compared to the previous run, the result being:
	 *      Day 2   -> submission of 80 keys   -> 1 empty file generated (different than what is currently on S3)
	 *      Day 3   -> submission of 80 keys   -> 1 distributed containing 160 keys (different than 1 empty file on S3)
	 *      Day 4   -> submission of 80 keys   -> 1 empty file (different than 1 empty file on S3)
	 */
	@Test
	void files_once_published_to_objectstore_should_not_be_overriden_because_of_retention_or_shifting_policies() 
			throws IOException {
		
		LocalDate testStartDate = LocalDate.of(2020, Month.JULY, 1);
		
		createDiagnosisKeyTestData(
				testStartDate,  					//keep data in the past for consistency
				LocalDate.of(2020, Month.JULY, 4),
				80); 								// 80 cases per day
		
		assembleAndDistribute(testOutputFolder.newFolder("output-before-retention"));
		List<S3Object> filesBeforeRetention = getPublishedFiles();
		
		triggerRetentionPolicy(testStartDate);
		
		/*trigger second distrubution after data retention policies were applied */
		assembleAndDistribute(testOutputFolder.newFolder("output-after-retention"));
		List<S3Object> filesAfterRetention = getPublishedFiles();
		
		assertFilesNotChangedInObjectStore(filesBeforeRetention, filesAfterRetention);
	}

	private List<S3Object> getPublishedFiles() {
		return objectStoreAccess.getObjectsWithPrefix(distributionServiceConfig.getApi().getVersionPath());
	}

	private void assertFilesNotChangedInObjectStore(List<S3Object> filesBeforeRetention, 
													List<S3Object> filesAfterRetention) {

		Map<String, S3Object> afterRetentionMap = filesAfterRetention.stream()
	            .collect(Collectors.toMap(S3Object::getObjectName, s3object -> s3object));
		
		filesBeforeRetention.stream().forEach( previouslyPublished -> {
			S3Object secondPublishedVersion = afterRetentionMap.get(previouslyPublished.getObjectName());
			if(secondPublishedVersion == null || secondPublishedVersion.getCwaHash().equals(previouslyPublished.getCwaHash()))
					throw new AssertionError("Files have been changed on object store "
							+ "due to retention policy. Before: " + previouslyPublished.getObjectName() 
							+ "-" + secondPublishedVersion.getCwaHash()
							+ "| After:" + secondPublishedVersion.getObjectName()  
							+ "-" + secondPublishedVersion.getCwaHash());
		});
	}

	private void triggerRetentionPolicy(LocalDate testStartDate) {
		//remove data from test start date
		//TODO: Refactor and find a better way to call the retention policy with mock config
		DistributionServiceConfig mockDistributionConfig = new DistributionServiceConfig();
		mockDistributionConfig.setRetentionDays(numberOfDaysSince(testStartDate));
		new RetentionPolicy( diagnosisKeyService, applicationContext, mockDistributionConfig,
							 s3RetentionPolicy).run(null);
	}

	private Integer numberOfDaysSince(LocalDate testStartDate) {
		return (int) ChronoUnit.DAYS.between(testStartDate, LocalDate.now()) - 1;
	}

	private void createDiagnosisKeyTestData(LocalDate fromDay, LocalDate untilDay, int casesPerDay) {
		DiagnosisTestData testData = DiagnosisTestData.of(fromDay, untilDay, casesPerDay);
		diagnosisKeyService.saveDiagnosisKeys(testData.getDiagnosisKeys());
	}
	
	private void assembleAndDistribute(File output) throws IOException {
		Mockito.when(distributionDirectoryProvider.getDirectory()).thenReturn(new DirectoryOnDisk(output));
		Mockito.when(distributionDirectoryProvider.getFileOnDisk()).thenReturn(output);
		
		fileAssembler.run(null);
		
		S3Publisher s3Publisher = new S3Publisher(objectStoreAccess, 
												new FailedObjectStoreOperationsCounter(distributionServiceConfig),
												newAsyncExecutor(), distributionServiceConfig);
		s3Publisher.publish(distributionDirectoryProvider.getFileOnDisk().toPath().toAbsolutePath());
	}
	
	private ThreadPoolTaskExecutor newAsyncExecutor() {
	    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
	    executor.setCorePoolSize(8);
	    executor.setMaxPoolSize(8);
	    executor.setQueueCapacity(11);
	    executor.initialize();
	    return executor;
	}
}
