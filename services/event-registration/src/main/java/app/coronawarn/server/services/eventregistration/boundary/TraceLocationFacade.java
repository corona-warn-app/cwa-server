package app.coronawarn.server.services.eventregistration.boundary;

import app.coronawarn.server.common.protocols.internal.pt.SignedTraceLocation;
import app.coronawarn.server.common.protocols.internal.pt.TraceLocation;
import app.coronawarn.server.services.eventregistration.service.TraceLocationService;
import app.coronawarn.server.services.eventregistration.service.TraceLocationSigningService;
import app.coronawarn.server.services.eventregistration.service.UuidHashGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;

@Service
public class TraceLocationFacade {

  private final TraceLocationService traceLocationService;
  private final TraceLocationSigningService signingService;

  public TraceLocationFacade(TraceLocationService traceLocationService,
      TraceLocationSigningService signingService) {
    this.traceLocationService = traceLocationService;
    this.signingService = signingService;
  }

  /**
   * Stores a trace location and returns the signed trace location in a callback.
   *
   * @param traceLocation               the trace location to save.
   * @param signedTraceLocationConsumer the callback to execute when the signed trace location is created.
   * @throws NoSuchAlgorithmException if the signing algorithm does not exist.
   */
  public void storeTraceLocation(TraceLocation traceLocation,
      Consumer<SignedTraceLocation> signedTraceLocationConsumer) throws NoSuchAlgorithmException {
    final String uuidHash = UuidHashGenerator.buildUuidHash();
    traceLocationService.saveTraceLocation(traceLocation, uuidHash);
    signedTraceLocationConsumer.accept(signingService.signTraceLocation(traceLocation, uuidHash));
  }

}
