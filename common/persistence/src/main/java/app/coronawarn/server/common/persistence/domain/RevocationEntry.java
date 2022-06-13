package app.coronawarn.server.common.persistence.domain;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
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
   * Hash for kid.
   *
   * @return hash
   * @see Arrays#hashCode(byte[])
   */
  public int getKidHashCode() {
    return Arrays.hashCode(getKid());
  }

  /**
   * Hash for kid.
   *
   * @return hash
   * @see Arrays#hashCode(byte[])
   */
  public int getKidTypeHashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(getKid());
    result = prime * result + Arrays.hashCode(getType());
    return result;
  }

  /**
   * Hash for X.
   *
   * @return hash
   * @see Arrays#hashCode(byte[])
   */
  public int getXHashCode() {
    return Arrays.hashCode(getXhash());
  }

  /**
   * Hash for X.
   *
   * @return hash
   * @see Arrays#hashCode(byte[])
   */
  public int getYHashCode() {
    return Arrays.hashCode(getYhash());
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

  /**
   * Return hexadecimal representation of {@link #getKid()} || {@link #getType()}.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%016x", new BigInteger(1, getKid())));
    sb.append(String.format("%02x", new BigInteger(1, getType())));
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RevocationEntry that = (RevocationEntry) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
