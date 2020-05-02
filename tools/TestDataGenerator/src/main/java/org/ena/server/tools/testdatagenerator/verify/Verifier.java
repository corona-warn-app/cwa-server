package org.ena.server.tools.testdatagenerator.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.ena.server.common.protocols.generated.Security.SignedPayload;
import org.ena.server.tools.testdatagenerator.common.Common;

public class Verifier {

  static void verify(File testDirectory, File expectedCertificateFile)
      throws IOException, CertificateException {
    Certificate expectedCertificate = Common.getCertificateFromFile(expectedCertificateFile);
    File daysDirectory = new File(testDirectory.toPath() + "/days");
    File hoursDirectory = new File(testDirectory.toPath() + "/hours");
    List<File> testFiles = new ArrayList<>();
    testFiles.addAll(Arrays.asList(Objects.requireNonNull(daysDirectory.listFiles())));
    testFiles.addAll(Arrays.asList(Objects.requireNonNull(hoursDirectory.listFiles())));
    testFiles.stream()
        .map(File::toPath)
        .map(Common.uncheckedFunction(Files::readAllBytes))
        .map(Common.uncheckedFunction(SignedPayload::parseFrom))
        .forEach(Common.uncheckedConsumer(signedPayload -> {
          Certificate payloadCertificate = Common.getCertificateFromBytes(
              signedPayload.getCertificateChain().toByteArray()
          );
          if (!payloadCertificate.equals(expectedCertificate)) {
            throw new CertificateException(
                "The payload certificate does not match the expected certificate."
            );
          }
          Signature payloadSignature = Signature.getInstance("Ed25519", "BC");
          payloadSignature.initVerify(payloadCertificate);
          payloadSignature.update(signedPayload.getPayload().toByteArray());
          if (!payloadSignature.verify(signedPayload.getSignature().toByteArray())) {
            throw new CertificateException(
                "The payload signature does not match the payload."
            );
          }
        }));
    System.out.println("All files OK");
  }
}
