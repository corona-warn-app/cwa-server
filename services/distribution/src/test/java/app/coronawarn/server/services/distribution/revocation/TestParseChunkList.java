package app.coronawarn.server.services.distribution.revocation;

import static app.coronawarn.server.common.shared.util.SerializationUtils.jsonExtractCosePayload;
import static org.junit.Assert.assertNotNull;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.objectstore.client.ObjectStorePublishingConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class})
@SpringBootTest(classes = {ObjectStorePublishingConfig.class})
@EnableConfigurationProperties(value = DistributionServiceConfig.class)
public class TestParseChunkList {

  @Test
  void cborToJson() throws IOException, ParseException {
    InputStream input = getClass().getResourceAsStream("/revocation/chunk.lst");
    assertNotNull("'/revocation/chunk.lst' not found! ", input);

    ObjectMapper mapper = new ObjectMapper();
    JSONObject jsonPayload = jsonExtractCosePayload(input.readAllBytes());
    Assertions.assertNotNull(jsonPayload);


    jsonPayload.forEach((keyAndType, values) -> {
      byte[] kid = keyAndType.toString().substring(0, keyAndType.toString().length() - 1).getBytes();
      byte[] type = keyAndType.toString().substring(keyAndType.toString().length() - 1).getBytes();
      List<String> valuesArray = new ArrayList<>();
      try {
        valuesArray = mapper.readValue(values.toString(), new TypeReference<>() {
        });
      } catch (IOException e) {
        e.printStackTrace();
      }

      Assertions.assertNotNull(kid);
      Assertions.assertNotNull(type);
      valuesArray.forEach(hash -> {
        Assertions.assertNotNull(hash.substring(0,2));
        Assertions.assertNotNull(hash.substring(2,4));
      });
    });
  }
}


