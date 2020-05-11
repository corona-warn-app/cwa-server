package app.coronawarn.server.tools.testdatagenerator.decorators.file;

import app.coronawarn.server.common.protocols.internal.SignedPayload;
import app.coronawarn.server.tools.testdatagenerator.interfaces.File;
import app.coronawarn.server.tools.testdatagenerator.util.Crypto;
import com.google.protobuf.ByteString;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.Stack;

/**
 * A {@link FileDecorator} that will convert the contents of a File into {@link
 * app.coronawarn.server.common.protocols.internal.SignedPayload}.
 */
public class SigningDecorator extends FileDecorator {

  final Crypto crypto;

  public SigningDecorator(File file, Crypto crypto) {
    super(file);
    this.crypto = crypto;
  }

  @Override
  public void prepare(Stack<Object> indices) {
    System.out.println("Signing \t\t\t" + this.getFileOnDisk().getPath());
    SignedPayload signedPayload = sign(this.getBytes(), crypto.getPrivateKey(),
        crypto.getCertificate());
    this.setBytes(signedPayload.toByteArray());
    super.prepare(indices);
  }

  public static SignedPayload sign(byte[] payload, PrivateKey privateKey, Certificate certificate) {
    try {
      Signature payloadSignature = Signature.getInstance("Ed25519", "BC");
      payloadSignature.initSign(privateKey);
      payloadSignature.update(payload);
      return SignedPayload.newBuilder()
          .setCertificateChain(ByteString.copyFrom(certificate.getEncoded()))
          .setSignature(ByteString.copyFrom(payloadSignature.sign()))
          .setPayload(ByteString.copyFrom(payload))
          .build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
