package app.coronawarn.server.services.eventregistration.boundary;

import static app.coronawarn.server.services.eventregistration.config.UrlConstants.TRACE_LOCATION_ROUTE;
import static app.coronawarn.server.services.eventregistration.config.UrlConstants.V1;

import app.coronawarn.server.common.protocols.internal.pt.TraceLocation;
import app.coronawarn.server.services.eventregistration.boundary.validation.ValidTraceLocation;
import app.coronawarn.server.services.eventregistration.service.TraceLocationService;
import org.aspectj.weaver.tools.Trace;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;


@RestController
@RequestMapping(V1)
@Validated
public class TraceLocationController {

  TraceLocationService traceLocationService;

  public TraceLocationController(TraceLocationService traceLocationService) {
    this.traceLocationService = traceLocationService;
  }

  /**
   * Entrypoint for creating new trace location.
   *
   * @param traceLocation incoming trace location that will be validated and persisted.
   * @return a response entity indicating whether the creation was successful or not.
   */
  @PostMapping(path = TRACE_LOCATION_ROUTE, consumes = "application/x-protobuf")
  public DeferredResult<ResponseEntity<Void>> createTraceLocation(
      @ValidTraceLocation TraceLocation traceLocation) throws NoSuchAlgorithmException {
    DeferredResult<ResponseEntity<Void>> result = new DeferredResult<>();
    traceLocationService.saveTraceLocation(traceLocation, 0);
    result.setResult(ResponseEntity.noContent().build());
    return result;
  }


}
