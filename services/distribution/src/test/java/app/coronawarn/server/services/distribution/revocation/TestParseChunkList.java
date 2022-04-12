package app.coronawarn.server.services.distribution.revocation;

import static app.coronawarn.server.common.shared.util.SerializationUtils.jsonExtractCosePayload;
import static org.junit.Assert.assertNotNull;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStorePublishingConfig;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith({SpringExtension.class})
@SpringBootTest(classes = {ObjectStorePublishingConfig.class})
@EnableConfigurationProperties(value = DistributionServiceConfig.class)
public class TestParseChunkList {

  @Test
  void cborToMap() throws IOException, ParseException {
    InputStream input = getClass().getResourceAsStream("/revocation/chunk.lst");
    assertNotNull("'/revocation/chunk.lst' not found! ", input);

    Map<byte[], List<byte[]>> payloadEntries = jsonExtractCosePayload(input.readAllBytes());
    Assertions.assertNotNull(payloadEntries);
    ArrayList<RevocationEntry> revocationEntries = new ArrayList<>();

    payloadEntries.forEach((keyAndType, values) -> {
      byte[] kid = Arrays.copyOfRange(keyAndType, 0, keyAndType.length - 1);
      byte[] type = Arrays.copyOfRange(keyAndType, keyAndType.length - 1, keyAndType.length);
      Assertions.assertNotNull(kid);
      Assertions.assertNotNull(type);

      values.forEach(hash -> {
        Assertions.assertNotNull(Arrays.copyOfRange(hash, 0, 2));
        Assertions.assertNotNull(Arrays.copyOfRange(hash, 2, 4));
      });
    });
  }

}


