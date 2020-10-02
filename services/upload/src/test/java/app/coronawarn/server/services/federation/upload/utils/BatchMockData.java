

package app.coronawarn.server.services.federation.upload.utils;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKeyBatch;
import com.google.protobuf.ByteString;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BatchMockData {

  public static DiagnosisKeyBatch makeSingleKeyBatch() {
    return makeBatch(List.of(makeDiagnosisKey()));
  }

  public static DiagnosisKeyBatch makeBatch(List<DiagnosisKey> keys) {
    return DiagnosisKeyBatch.newBuilder()
        .addAllKeys(keys).build();
  }

  public static DiagnosisKey makeDiagnosisKey() {
    byte[] bytes = new byte[16];
    ThreadLocalRandom.current().nextBytes(bytes);
    return DiagnosisKey.newBuilder().setKeyData(ByteString.copyFrom(bytes)).build();
  }

}
