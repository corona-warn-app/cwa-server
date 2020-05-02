package org.ena.server.tools.testdatagenerator.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.ena.server.common.protocols.generated.ExposureKeys.TemporaryExposureKeyBucket.AggregationInterval;

public class Common {

  public static int getRandomBetween(int minIncluding, int maxIncluding) {
    return ThreadLocalRandom.current().nextInt(
        minIncluding,
        maxIncluding + 1
    );
  }

  public static long getRandomBetween(long minIncluding, long maxIncluding) {
    return ThreadLocalRandom.current().nextLong(
        minIncluding,
        maxIncluding + 1
    );
  }

  public static <T, R> Function<T, R> uncheckedFunction(
      CheckedFunction<T, R, ? extends Exception> function) {
    return t -> {
      try {
        return function.apply(t);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  public static <T> Consumer<T> uncheckedConsumer(
      CheckedConsumer<T, ? extends Exception> function) {
    return t -> {
      try {
        function.apply(t);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  public static PrivateKey getPrivateKeyFromFile(File privateKeyPath) throws IOException {
    PEMParser pemParser =
        new PEMParser(Files.newBufferedReader(privateKeyPath.toPath(), StandardCharsets.UTF_8));
    PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) pemParser.readObject();
    return new JcaPEMKeyConverter().getPrivateKey(privateKeyInfo);
  }

  public static Certificate getCertificateFromFile(File certificatePath)
      throws IOException, CertificateException {
    return getCertificateFromBytes(
        getBytesFromFile(certificatePath)
    );
  }

  public static Certificate getCertificateFromBytes(byte[] bytes) throws CertificateException {
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    InputStream certificateByteStream = new ByteArrayInputStream(bytes);
    return certificateFactory.generateCertificate(certificateByteStream);
  }

  public static byte[] getBytesFromFile(File file) throws IOException {
    return Files.readAllBytes(file.toPath());
  }

  public static String getOutputFileName(String shardKey, String schemaVersion,
      AggregationInterval aggregationInterval, long timestampEpoch) {
    long aggregationMinutes = aggregationInterval == AggregationInterval.HOURLY
        ? TimeUnit.HOURS.toMinutes(1)
        : TimeUnit.DAYS.toMinutes(1);
    return shardKey + "_" + schemaVersion + "_" + aggregationMinutes + "m_" + timestampEpoch
        + ".cov";
  }

  /**
   * Convert checked exceptions to unchecked exceptions in Functions.
   */
  @FunctionalInterface
  public interface CheckedFunction<T, R, E extends Exception> {

    R apply(T t) throws E;
  }

  /**
   * Convert checked exceptions to unchecked exceptions in Consumers.
   */
  @FunctionalInterface
  public interface CheckedConsumer<T, E extends Exception> {

    void apply(T t) throws E;
  }
}
