package app.coronawarn.server.services.distribution.revocation;

import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonTokenId;
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
    StringWriter stringWriter2 = new StringWriter();
    JsonGenerator jsonGenerator2 = jsonFactory.createGenerator(stringWriter);
    int i = 0;
    while (cborParser.nextToken() != null) {
      if (JsonTokenId.ID_EMBEDDED_OBJECT == cborParser.getCurrentToken().id() && i < 2 /* 3rd ebbeded object is the signature */) {
        byte[] obj = (byte[]) cborParser.getEmbeddedObject();
        CBORParser payload = cborFactory.createParser(obj);
        while (payload.nextToken() != null) {
          jsonGenerator2.copyCurrentEvent(payload);
        }
        i++;
      }
      jsonGenerator.copyCurrentEvent(cborParser);
    }
    jsonGenerator.flush();
    jsonGenerator2.flush();
    System.out.println(stringWriter.toString());
    System.out.println(stringWriter2.toString());
  }
}
