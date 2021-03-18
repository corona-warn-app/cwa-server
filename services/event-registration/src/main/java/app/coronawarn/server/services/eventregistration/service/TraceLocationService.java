package app.coronawarn.server.services.eventregistration.service;

import app.coronawarn.server.common.protocols.internal.pt.TraceLocation;
import app.coronawarn.server.services.eventregistration.config.EventRegistrationConfiguration;
import app.coronawarn.server.services.eventregistration.repository.TraceLocationRepository;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Component
public class TraceLocationService {

  Logger logger = LoggerFactory.getLogger(TraceLocationService.class);
  private EventRegistrationConfiguration eventRegistrationConfiguration;

  public TraceLocationService(EventRegistrationConfiguration eventRegistrationConfiguration,
      TraceLocationRepository traceLocationRepository) {
    this.eventRegistrationConfiguration = eventRegistrationConfiguration;
    this.traceLocationRepository = traceLocationRepository;
  }

  private TraceLocationRepository traceLocationRepository;

  public void saveTraceLocation(TraceLocation traceLocation, int index) throws
      NoSuchAlgorithmException {

    try {
      String guid = UuidHashGenerator.buildUuidHash();
      traceLocationRepository.save(guid, traceLocation.getVersion(),
          LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
    } catch (DuplicateKeyException e) {
      if (index >= eventRegistrationConfiguration.getSaveRetriesLimit()) {
        logger.error("Trying to save Trace Location object has failed. The limit has been exceeded!");
      }
      index++;
    }

  }
}
