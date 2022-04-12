package app.coronawarn.server.common.persistence.domain;


import java.util.Arrays;
import org.springframework.data.annotation.Id;

public class RevocationEntry {

  @Id
  private long id;
  private final byte[] kid;
  private final byte[] type;
  private final byte[] hash;
  private final byte[] xhash;
  private final byte[] yhash;

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
    this.kid = kid;
    this.type = type;
    this.hash = hash;
    this.xhash = xhash;
    this.yhash = yhash;
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

  public byte[] getKid() {
    return kid;
  }

  public byte[] getType() {
    return type;
  }

  public byte[] getHash() {
    return hash;
  }

  public byte[] getXhash() {
    return xhash;
  }

  public byte[] getYhash() {
    return yhash;
  }

  public long getId() {
    return id;
  }
}
