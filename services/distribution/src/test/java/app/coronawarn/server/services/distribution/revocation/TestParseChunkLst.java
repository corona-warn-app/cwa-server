package app.coronawarn.server.services.distribution.revocation;

import static app.coronawarn.server.common.shared.util.SerializationUtils.jsonExtractCosePayload;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;

public class TestParseChunkLst {

  @Test
  void cborToJson() throws IOException, ParseException {
    InputStream input = getClass().getResourceAsStream("/revocation/chunk.lst");
    assertNotNull("'/revocation/chunk.lst' not found! ", input);

    ObjectMapper mapper = new ObjectMapper();
    JSONObject jsonPayload = jsonExtractCosePayload(input.readAllBytes());

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
      System.out.println(kid);
      System.out.println(type);
      valuesArray.forEach(hash -> {
        System.out.println(hash.substring(0,2));
        System.out.println(hash.substring(2,4));
      });
    });
  }
}


