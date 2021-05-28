package app.coronawarn.server.services.federation.upload.integration;

import app.coronawarn.server.services.federation.upload.config.UploadServiceConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({ "integration-test", "disable-ssl-efgs-verification", "connect-chgs" })
@EnableConfigurationProperties(value = UploadServiceConfig.class)
@DirtiesContext
@SpringBootTest
public class UploadKeyChgsIT extends DiagnosisKeyUploadIT {
}