package app.coronawarn.server.services.distribution.assembly.certrules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;

public class CborTest {

  public CborTest() throws JsonProcessingException {
    ObjectMapper mapper = new CBORMapper();
    byte[] cborData = mapper.writeValueAsBytes(null);
  }
}
