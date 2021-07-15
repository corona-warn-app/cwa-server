package app.coronawarn.server.common.shared.util;

public class CwaStringUtils {

  private CwaStringUtils() {
  }

  public static char[] emptyCharrArrayIfNull(String input) {
    return input != null ? input.toCharArray() : new char[] {};
  }
}
