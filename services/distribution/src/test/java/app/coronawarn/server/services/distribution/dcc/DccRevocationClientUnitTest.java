package app.coronawarn.server.services.distribution.dcc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.dcc.decode.DccRevocationListDecodeException;
import app.coronawarn.server.services.distribution.dcc.decode.DccRevocationListDecoder;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class DccRevocationClientUnitTest {

  ProdDccRevocationClient prodDccRevocationClient;

  TestDccRevocationClient testDccRevocationClient;

  @Mock
  ResourceLoader resourceLoader;

  @Mock
  DccRevocationListDecoder dccRevocationListDecoder;

  @Mock
  DccRevocationFeignClient dccRevocationFeignClient;

  @BeforeEach
  void setup() {
    testDccRevocationClient = new TestDccRevocationClient(resourceLoader, dccRevocationListDecoder);
    prodDccRevocationClient = new ProdDccRevocationClient(dccRevocationFeignClient, dccRevocationListDecoder);
  }

  @Test
  void shouldThrowDccRevocationListFetchException() {
    when(resourceLoader.getResource(any())).thenThrow(NullPointerException.class);
    assertThrows(FetchDccListException.class, () -> testDccRevocationClient.getDccRevocationList());
  }

  @Test
  void shouldReturnEmptyWhenThrowDccDecodeException() throws Exception {
    when(dccRevocationFeignClient.getRevocationList()).thenReturn(ResponseEntity.ok().body(new byte[]{}));
    when(dccRevocationListDecoder.decode(any())).thenThrow(DccRevocationListDecodeException.class);
    assertEquals(prodDccRevocationClient.getDccRevocationList(), Optional.empty());
  }
}
