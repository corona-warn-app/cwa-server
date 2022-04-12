package app.coronawarn.server.common.persistence.domain;

import java.io.Serializable;
import java.util.Arrays;

public class RevocationEntryId implements Serializable {

  private static final long serialVersionUID = -2082474530786365799L;

  private byte[] kid;
  private byte[] type;
  private byte[] hash;

  public RevocationEntryId() {
  }

  /**
   * Primary Key.
   * 
   * @param kid kid
   * @param type type
   * @param hash hash
   */
  public RevocationEntryId(final byte[] kid, final byte[] type, final byte[] hash) {
    this.kid = kid;
    this.type = type;
    this.hash = hash;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }
    final RevocationEntryId other = (RevocationEntryId) obj;
    return Arrays.equals(hash, other.hash) && Arrays.equals(kid, other.kid) && Arrays.equals(type, other.type);
  }

  public byte[] getHash() {
    return hash;
  }

  public byte[] getKid() {
    return kid;
  }

  public byte[] getType() {
    return type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(hash);
    result = prime * result + Arrays.hashCode(kid);
    result = prime * result + Arrays.hashCode(type);
    return result;
  }

  public void setHash(final byte[] hash) {
    this.hash = hash;
  }

  public void setKid(final byte[] kid) {
    this.kid = kid;
  }

  public void setType(final byte[] type) {
    this.type = type;
  }
}
