package app.coronawarn.server.services.eventregistration.boundary;

import static app.coronawarn.server.services.eventregistration.config.UrlConstants.TRACE_LOCATION_ROUTE;
import static app.coronawarn.server.services.eventregistration.config.UrlConstants.V1;

import app.coronawarn.server.common.protocols.internal.pt.SignedTraceLocation;
import app.coronawarn.server.common.protocols.internal.pt.TraceLocation;
import app.coronawarn.server.services.eventregistration.boundary.validation.ValidTraceLocation;
import java.security.NoSuchAlgorithmException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;


@RestController
@RequestMapping(V1)
@Validated
public class TraceLocationController {

  private final TraceLocationFacade traceLocationFacade;


  public TraceLocationController(TraceLocationFacade traceLocationFacade) {
    this.traceLocationFacade = traceLocationFacade;

  }

  /**
   * Entrypoint for creating new trace location.
   *
   * @param traceLocation incoming trace location that will be validated and persisted.
   * @return a response entity indicating whether the creation was successful or not.
   */
  @PostMapping(path = TRACE_LOCATION_ROUTE, consumes = "application/x-protobuf")
  public DeferredResult<ResponseEntity<SignedTraceLocation>> createTraceLocation(
      @ValidTraceLocation @RequestBody TraceLocation traceLocation) throws NoSuchAlgorithmException {
    DeferredResult<ResponseEntity<SignedTraceLocation>> result = new DeferredResult<>();
    traceLocationFacade.storeTraceLocation(traceLocation,
        signedTraceLocation -> result
            .setResult(ResponseEntity.status(HttpStatus.CREATED).body(signedTraceLocation)));
    return result;
  }


}
