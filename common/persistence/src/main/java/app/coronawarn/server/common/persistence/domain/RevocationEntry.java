package app.coronawarn.server.common.persistence.domain;

import java.util.Arrays;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Embedded.OnEmpty;

public class RevocationEntry {

  @Id
  @Embedded(onEmpty = OnEmpty.USE_NULL)
  private RevocationEntryId id;

  public RevocationEntry() {
  }

  /**
   * DCC Revocation Entry.
   *
   * @param kid  byte sequence of the key except for the last byte
   * @param type last byte of the key
   * @param hash byte sequence of the item
   */
  public RevocationEntry(final byte[] kid, final byte[] type, final byte[] hash) {
    id = new RevocationEntryId(kid, type, hash);
  }

  public byte[] getHash() {
    return id.getHash();
  }

  public byte[] getKid() {
    return id.getKid();
  }

  /**
   * Hash for kid and type.
   *
   * @see Arrays#hashCode(byte[])
   * @return hash
   */
  public int getKidHash() {
    return Arrays.hashCode(getKid());
  }

  public byte[] getType() {
    return id.getType();
  }

  public byte[] getXhash() {
    return Arrays.copyOfRange(getHash(), 0, 1);
  }

  public byte[] getYhash() {
    return Arrays.copyOfRange(getHash(), 1, 2);
  }
}
