package app.coronawarn.server.services.distribution.dcc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import feign.Request;
import feign.Request.Body;
import feign.Request.HttpMethod;
import feign.Request.Options;
import feign.RequestTemplate;
import feign.Response;
import feign.httpclient.ApacheHttpClient;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class DccRevocationClientDelegatorTest {

  @Test
  void nullBodyShouldBeTurnedIntoEmptyString() throws Exception {
    final ApacheHttpClient client = mock(ApacheHttpClient.class);
    final Request request = Request.create(HttpMethod.GET, "http://localhost", Collections.emptyMap(), Body.empty(),
        (RequestTemplate) null);
    final Response mockResponse = Response.builder().request(request).body((Response.Body) null).build();
    when(client.execute(any(), any())).thenReturn(mockResponse);
    final DccRevocationClientDelegator fixture = new DccRevocationClientDelegator(client);
    assertNull(mockResponse.body());
    final Response response = fixture.execute(request, new Options());
    assertNotNull(response.body());
  }

  @Test
  void responseIsNotChangedIfBodyIsNotNull() throws Exception {
    final ApacheHttpClient client = mock(ApacheHttpClient.class);
    final Request request = Request.create(HttpMethod.GET, "http://localhost", Collections.emptyMap(), Body.empty(),
        (RequestTemplate) null);
    final Response mockResponse = Response.builder().request(request).body("foo".getBytes()).build();
    when(client.execute(any(), any())).thenReturn(mockResponse);
    final DccRevocationClientDelegator fixture = new DccRevocationClientDelegator(client);
    assertNotNull(mockResponse.body());
    final Response response = fixture.execute(request, new Options());
    assertEquals(mockResponse, response);
  }

  @Test
  void testDccRevocationClientDelegator() {
    final DccRevocationClientDelegator fixture = new DccRevocationClientDelegator(new ApacheHttpClient());
    assertNotNull(fixture);
  }
}
