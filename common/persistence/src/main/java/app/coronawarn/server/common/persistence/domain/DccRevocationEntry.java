package app.coronawarn.server.common.persistence.domain;

public class DccRevocationEntry {

  private final byte[] kid;
  private final byte[] type;
  private final byte[] hash;
  private final byte[] x;
  private final byte[] y;

  public DccRevocationEntry(byte[] kid, byte[] type, byte[] hash, byte[] x, byte[] y) {
    this.kid = kid;
    this.type = type;
    this.hash = hash;
    this.x = x;
    this.y = y;
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

  public byte[] getX() {
    return x;
  }

  public byte[] getY() {
    return y;
  }
}
