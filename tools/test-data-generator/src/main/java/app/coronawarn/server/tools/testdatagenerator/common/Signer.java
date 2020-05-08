package app.coronawarn.server.tools.testdatagenerator.common;

import app.coronawarn.server.common.protocols.internal.SignedPayload;
import com.google.protobuf.ByteString;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;

public class Signer {

  public static SignedPayload sign(byte[] payload, PrivateKey privateKey, Certificate certificate)
      throws CertificateEncodingException, InvalidKeyException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException {
    Signature payloadSignature = Signature.getInstance("Ed25519", "BC");
    payloadSignature.initSign(privateKey);
    payloadSignature.update(payload);
    return SignedPayload.newBuilder()
        .setCertificateChain(ByteString.copyFrom(certificate.getEncoded()))
        .setSignature(ByteString.copyFrom(payloadSignature.sign()))
        .setPayload(ByteString.copyFrom(payload))
        .build();
  }
}
