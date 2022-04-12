package app.coronawarn.server.common.persistence.domain;

import java.util.Arrays;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Embedded.OnEmpty;

public class RevocationEntry {

  @Id
  @Embedded(onEmpty = OnEmpty.USE_NULL)
  private RevocationEntryId id;

  /**
   * DCC Revocation Entry.
   *
   * @param kid   byte sequence of the key except for the last byte
   * @param type  last byte of the key
   * @param hash  byte sequence of the item
   * @param xhash first byte of hash
   * @param yhash second byte of hash
   */
  public RevocationEntry(byte[] kid, byte[] type, byte[] hash, byte[] xhash, byte[] yhash) {
    id = new RevocationEntryId(kid, type, hash);
  }

  /**
   * DCC Revocation Entry.
   *
   * @param kid  byte sequence of the key except for the last byte
   * @param type last byte of the key
   * @param hash byte sequence of the item
   */
  public RevocationEntry(byte[] kid, byte[] type, byte[] hash) {
    id = new RevocationEntryId(kid, type, hash);
  }

  /**
   * Hash for kid and type.
   * @return hash
   */
  public int getKidHash() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(kid);
    return result;
  }
  public RevocationEntry() {
  }

  public byte[] getKid() {
    return id.getKid();
  }

  public byte[] getType() {
    return id.getType();
  }

  public byte[] getHash() {
    return id.getHash();
  }

  public byte[] getXhash() {
    return Arrays.copyOfRange(getHash(), 0, 1);
  }

  public byte[] getYhash() {
    return Arrays.copyOfRange(getHash(), 1, 2);
  }
}
