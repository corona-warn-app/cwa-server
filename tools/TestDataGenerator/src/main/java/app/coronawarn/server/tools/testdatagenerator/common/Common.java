package app.coronawarn.server.tools.testdatagenerator.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Common {

  public static int getRandomBetween(int minIncluding, int maxIncluding, Random random) {
    return Math.toIntExact(getRandomBetween(
        (long) minIncluding,
        (long) maxIncluding,
        random
    ));
  }

  public static long getRandomBetween(long minIncluding, long maxIncluding, Random random) {
    return minIncluding + (long) (random.nextDouble() * (maxIncluding - minIncluding));
  }

  public static int nextPoisson(int mean, Random random) {
    // https://stackoverflow.com/a/9832977
    // https://en.wikipedia.org/wiki/Poisson_distribution#Generating_Poisson-distributed_random_variables
    double L = Math.exp(-mean);
    int k = 0;
    double p = 1.0;
    do {
      p = p * random.nextDouble();
      k++;
    } while (p > L);
    return k - 1;
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

  public static File makeDirectory(File root, String name) {
    File directory = new File(root.getPath() + "/" + name);
    directory.mkdirs();
    return directory;
  }

  public static File makeFile(File root, String name) {
    File directory = new File(root.getPath() + "/" + name);
    try {
      directory.createNewFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return directory;
  }

  public static void writeJson(File to, JSONAware json) {
    try {
      FileWriter file = new FileWriter(to.getPath());
      file.write(json.toJSONString());
      file.flush();
      file.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void writeBytesToFile(byte[] bytes, File outputFile) {
    try {
      outputFile.createNewFile();
      FileOutputStream outputFileStream = new FileOutputStream(outputFile);
      outputFileStream.write(bytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> void writeIndex(DirectoryIndex<T> directoryIndex, Function<Object, ?> formatter) {
    File file = makeFile(directoryIndex.directory, "index");
    JSONArray array = new JSONArray();
    List<?> elements = directoryIndex.index.stream()
        .map(formatter)
        .collect(Collectors.toList());
    array.addAll(elements);
    writeJson(file, array);
  }

  public static void writeIndex(File directory, List<?> index) {
    writeIndex(new DirectoryIndex<>(directory, index), Object::toString);
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

  /*
  @FunctionalInterface
  public interface Formatter<T, String> extends Function<T, String> {
    String apply(T t);
  }
  */

  public static class DirectoryIndex<T> {
    public File directory;
    public List<T> index;

    public DirectoryIndex(File directory, List<T> index) {
      this.directory = directory;
      this.index = index;
    }
  }
}
