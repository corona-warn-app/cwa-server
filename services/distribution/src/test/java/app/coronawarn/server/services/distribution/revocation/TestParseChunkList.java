package app.coronawarn.server.services.distribution.revocation;

import static org.junit.Assert.assertNotNull;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dcc.DccRevocationClient;
import app.coronawarn.server.services.distribution.dcc.FetchDccListException;
import app.coronawarn.server.services.distribution.dcc.TestDccRevocationClient;
import app.coronawarn.server.services.distribution.dcc.decode.DccRevocationListDecoder;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStorePublishingConfig;
import java.io.IOException;
import java.io.InputStream;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith({SpringExtension.class})
@SpringBootTest(classes = {ObjectStorePublishingConfig.class, TestDccRevocationClient.class,
    DccRevocationListDecoder.class})
@ActiveProfiles("fake-dcc-revocation")
@EnableConfigurationProperties(value = DistributionServiceConfig.class)
public class TestParseChunkList {

  @Autowired
  TestDccRevocationClient dccRevocationClient;

  @Test
  void cborToMap() throws IOException, ParseException, FetchDccListException {
    dccRevocationClient.getDccRevocationList().ifPresent(payloadEntries -> {

      payloadEntries.forEach(revocationEntry -> {
        Assert.assertNotNull(revocationEntry);
        Assert.assertNotNull(revocationEntry.getKid());
        Assert.assertEquals(revocationEntry.getType().length, 1);
        Assert.assertNotNull(revocationEntry.getHash());
      });
    });
  }

}


