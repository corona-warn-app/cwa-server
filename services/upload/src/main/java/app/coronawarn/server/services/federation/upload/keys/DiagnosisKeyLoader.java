package app.coronawarn.server.services.federation.upload.keys;


import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import java.util.List;

public interface DiagnosisKeyLoader {

  List<DiagnosisKey> loadDiagnosisKeys();

}
