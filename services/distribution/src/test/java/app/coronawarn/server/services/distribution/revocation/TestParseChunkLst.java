package app.coronawarn.server.services.distribution.revocation;

import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

public class TestParseChunkLst {

  @Test
  void cborToJson() throws IOException {
    InputStream input = getClass().getResourceAsStream("/revocation/chunk.lst");
    assertNotNull("'/revocation/chunk.lst' not found! ", input);

    CBORFactory cborFactory = new CBORFactory();
    CBORParser cborParser = cborFactory.createParser(input);
    JsonFactory jsonFactory = new JsonFactory();
    StringWriter stringWriter = new StringWriter();
    JsonGenerator jsonGenerator = jsonFactory.createGenerator(stringWriter);
    while (cborParser.nextToken() != null) {
      jsonGenerator.copyCurrentEvent(cborParser);
    }
    jsonGenerator.flush();
    System.out.println(stringWriter.toString());
  }
}
