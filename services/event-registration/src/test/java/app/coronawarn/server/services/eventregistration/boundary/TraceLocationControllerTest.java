package app.coronawarn.server.services.eventregistration.boundary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import app.coronawarn.server.common.protocols.internal.pt.TraceLocation;
import static app.coronawarn.server.services.eventregistration.config.UrlConstants.TRACE_LOCATION_ROUTE;
import static app.coronawarn.server.services.eventregistration.config.UrlConstants.V1;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TraceLocationControllerTest {


  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    this.mockMvc = standaloneSetup(TraceLocationController.class).build();
  }


  @Test
  public void shouldReturnNoContent() throws Exception {
    final TraceLocation build = TraceLocation.newBuilder().build();
    final MvcResult mvcResult = this.mockMvc.perform(
        post(V1 + TRACE_LOCATION_ROUTE)
            .contentType("application/x-protobuf")
            .content(build.toByteArray()))
        .andExpect(request().asyncStarted())
        .andReturn();

    this.mockMvc.perform(asyncDispatch(mvcResult)).andExpect(status().isNoContent());
  }

}
