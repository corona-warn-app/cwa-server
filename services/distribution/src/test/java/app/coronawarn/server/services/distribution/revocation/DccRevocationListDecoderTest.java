package app.coronawarn.server.services.distribution.revocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import app.coronawarn.server.common.shared.util.SerializationUtils;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dcc.decode.DccRevocationListDecodeException;
import app.coronawarn.server.services.distribution.dcc.decode.DccRevocationListDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DccRevocationListDecoder.class}, initializers = ConfigDataApplicationContextInitializer.class)
public class DccRevocationListDecoderTest {

  @Autowired
  DccRevocationListDecoder dccRevocationListDecoder;

  @Test
  void testDccRevocationListDecoderShouldReturnListOfEntries() throws IOException, DccRevocationListDecodeException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("revocation/chunk.lst");
    List<RevocationEntry> revocationEntryList = dccRevocationListDecoder.decode(inputStream.readAllBytes());
    assertEquals(revocationEntryList.size(), 586);
  }

  @Test
  void testDccRevocationListDecoderShouldThrowException() {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("revocation/chunk.lst");
    try (MockedStatic<SerializationUtils> utilities = Mockito.mockStatic(SerializationUtils.class)) {
      utilities.when(() -> SerializationUtils.jsonExtractCosePayload(any()))
          .thenThrow(ParseException.class);
      assertThrows(DccRevocationListDecodeException.class,
          () -> dccRevocationListDecoder.decode(inputStream.readAllBytes()));
    }
  }
}
