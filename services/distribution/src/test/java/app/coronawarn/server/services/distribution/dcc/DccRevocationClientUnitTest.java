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
import org.springframework.core.io.ByteArrayResource;
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

  @Test
  void coverTestDccRevocationClient1() throws Exception {
    when(resourceLoader.getResource(any())).thenThrow(RuntimeException.class);
    assertThrows(FetchDccListException.class, () -> testDccRevocationClient.getDccRevocationList());
  }

  @Test
  void coverTestDccRevocationClient2() throws Exception {
    when(resourceLoader.getResource(any())).thenReturn(new ByteArrayResource("foo".getBytes()));
    when(dccRevocationListDecoder.decode(any())).thenThrow(DccRevocationListDecodeException.class);
    assertEquals(Optional.empty(), testDccRevocationClient.getDccRevocationList());
  }

  @Test
  void coverTestDccRevocationClient3() throws Exception {
    assertEquals("62620c23-2953", testDccRevocationClient.getETag());
  }

  @Test
  void getETagShouldThrowFetchDccListException() {
    when(dccRevocationFeignClient.head()).thenThrow(RuntimeException.class);
    assertThrows(FetchDccListException.class, () -> prodDccRevocationClient.getETag());
  }

  @BeforeEach
  void setup() {
    testDccRevocationClient = new TestDccRevocationClient(resourceLoader, dccRevocationListDecoder);
    prodDccRevocationClient = new ProdDccRevocationClient(dccRevocationFeignClient, dccRevocationListDecoder);
  }

  @Test
  void shouldReturnEmptyWhenThrowDccDecodeException() throws Exception {
    when(dccRevocationFeignClient.getRevocationList()).thenReturn(ResponseEntity.ok().body(new byte[] {}));
    when(dccRevocationListDecoder.decode(any())).thenThrow(DccRevocationListDecodeException.class);
    assertEquals(prodDccRevocationClient.getDccRevocationList(), Optional.empty());
  }

  @Test
  void shouldThrowFetchDccListException() {
    when(dccRevocationFeignClient.getRevocationList()).thenThrow(RuntimeException.class);
    assertThrows(FetchDccListException.class, () -> prodDccRevocationClient.getDccRevocationList());
  }
}
