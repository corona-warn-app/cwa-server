package app.coronawarn.server.tools.testdatagenerator.structure;

import app.coronawarn.server.tools.testdatagenerator.util.Common;
import app.coronawarn.server.tools.testdatagenerator.util.Crypto;
import app.coronawarn.server.tools.testdatagenerator.util.IOUtils;
import app.coronawarn.server.tools.testdatagenerator.util.Signer;
import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A {@link Directory} that will convert all of its {@link Directory#getFiles} files into {@link
 * app.coronawarn.server.common.protocols.internal.SignedPayload SignedPayloads} before writing
 * them.
 */
public interface SigningDirectory {

  void sign();

  default void signFiles(File directory, Crypto crypto) {

    Arrays.stream(Objects.requireNonNull(directory.listFiles()))
        .forEach(file -> Stream.of(file)
            .filter(File::isFile)
            .map(Common.uncheckedFunction(IOUtils::getBytesFromFile))
            .map(bytes -> {
              try {
                return Signer.sign(bytes, crypto.getPrivateKey(), crypto.getCertificate());
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
            .forEach(signedPayload -> IOUtils.writeBytesToFile(signedPayload.toByteArray(), file)));
  }
}
