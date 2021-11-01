package app.coronawarn.server.services.federation.upload.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"disable-ssl-efgs-verification", "connect-efgs"})
@DirtiesContext
@SpringBootTest
public class ReplicationForEfgsIT extends DiagnosisKeyReplicationIT {
}