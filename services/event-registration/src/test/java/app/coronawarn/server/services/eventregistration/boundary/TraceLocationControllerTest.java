package app.coronawarn.server.services.eventregistration.boundary;

import app.coronawarn.server.common.protocols.internal.evreg.TraceLocation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static app.coronawarn.server.services.eventregistration.config.UrlConstants.TRACE_LOCATION_ROUTE;
import static app.coronawarn.server.services.eventregistration.config.UrlConstants.V1;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TraceLocationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void shouldReturnNoContent() throws Exception {
    MvcResult mvcResult = this.mockMvc.perform(
        post(V1 + TRACE_LOCATION_ROUTE)
            .contentType("application/x-protobuf")
            .content(TraceLocation.newBuilder().build().toByteArray()))
        .andExpect(request().asyncStarted())
        .andReturn();

    this.mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isNoContent());
  }

}
