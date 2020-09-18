

package app.coronawarn.server.services.federation.upload.keys;


import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import java.util.List;

public interface DiagnosisKeyLoader {

  List<FederationUploadKey> loadDiagnosisKeys();

}
