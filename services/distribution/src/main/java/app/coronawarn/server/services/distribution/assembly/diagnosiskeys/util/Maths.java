package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.util;

public class Maths {

  public static int ceilDiv(int numerator, int denominator) {
    return (numerator + denominator - 1) / denominator;
  }
}
