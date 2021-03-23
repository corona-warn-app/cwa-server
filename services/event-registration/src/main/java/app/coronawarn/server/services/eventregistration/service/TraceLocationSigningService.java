package app.coronawarn.server.services.eventregistration.service;

import app.coronawarn.server.common.protocols.internal.pt.SignedTraceLocation;
import app.coronawarn.server.common.protocols.internal.pt.TraceLocation;
import app.coronawarn.server.services.eventregistration.config.EventRegistrationConfiguration;
import app.coronawarn.server.services.eventregistration.domain.errors.SigningException;
import com.google.protobuf.ByteString;
import java.security.GeneralSecurityException;
import java.security.Signature;
import org.springframework.stereotype.Service;

@Service
public class TraceLocationSigningService {

  private final CryptoProvider cryptoProvider;
  private final EventRegistrationConfiguration eventRegistrationConfiguration;


  public TraceLocationSigningService(CryptoProvider cryptoProvider, EventRegistrationConfiguration configuration) {
    this.cryptoProvider = cryptoProvider;
    this.eventRegistrationConfiguration = configuration;
  }

  /**
   * Builds a new TraceLocation based on the input and sets it's uuid.
   *
   * @param traceLocation the TraceLocation used as base prototype.
   * @param uuidHash      the uuid that was used previously to save a TraceLocation.
   * @return a signed TraceLocation (uuid signed).
   */
  public SignedTraceLocation signTraceLocation(TraceLocation traceLocation, String uuidHash) {
    try {
      TraceLocation filledTraceLocation = TraceLocation.newBuilder(traceLocation).setGuid(uuidHash).build();
      Signature payloadSignature = Signature
          .getInstance(eventRegistrationConfiguration.getSignature().getAlgorithmName(),
              eventRegistrationConfiguration.getSignature().getSecurityProvider());
      payloadSignature.initSign(cryptoProvider.getPrivateKey());
      payloadSignature.update(filledTraceLocation.getGuidBytes().toByteArray());
      return SignedTraceLocation.newBuilder()
          .setLocation(filledTraceLocation.toByteString())
          .setSignature(ByteString.copyFrom(payloadSignature.sign())).build();
    } catch (GeneralSecurityException e) {
      throw new SigningException("Failed to sign trace location.", e);
    }
  }

}
