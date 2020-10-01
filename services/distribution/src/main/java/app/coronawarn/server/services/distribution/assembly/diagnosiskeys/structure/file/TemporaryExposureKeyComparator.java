

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.file;

import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import com.google.protobuf.ByteString;
import java.util.Comparator;

/**
 * Sorts the keys based on the TEK key data.
 */
public class TemporaryExposureKeyComparator implements Comparator<TemporaryExposureKey> {

  private static final Comparator<ByteString> byteStringComparator = ByteString.unsignedLexicographicalComparator();

  @Override
  public int compare(TemporaryExposureKey o1, TemporaryExposureKey o2) {
    return byteStringComparator.compare(o1.getKeyData(), o2.getKeyData());
  }
}
