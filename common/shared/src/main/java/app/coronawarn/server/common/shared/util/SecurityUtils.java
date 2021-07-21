package app.coronawarn.server.common.shared.util;

import static java.util.Arrays.copyOfRange;

import app.coronawarn.server.common.shared.util.HashUtils.Algorithms;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequence;

public class SecurityUtils {

  private SecurityUtils() {
  }

  /**
   * TODO: add javadoc.
   */
  public static PublicKey getPublicKeyFromString(String publicKey)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] base64PublicKey = Base64.getDecoder().decode(publicKey);
    X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(base64PublicKey);

    return KeyFactory.getInstance(Algorithms.EC.getName()).generatePublic(x509EncodedKeySpec);
  }

  /**
   * TODO: add javadoc.
   */
  public static byte[] getEcdsaEncodeFromSignature(byte[] signature) throws IOException {
    byte[] left = copyOfRange(signature, 0, signature.length / 2);
    byte[] right = copyOfRange(signature, signature.length / 2, signature.length);

    ASN1EncodableVector encodableVector = new ASN1EncodableVector();
    encodableVector.add(new ASN1Integer(new BigInteger(1, left)));
    encodableVector.add(new ASN1Integer(new BigInteger(1, right)));

    DERSequence derSequence = new DERSequence(encodableVector);
    return derSequence.getEncoded();
  }

  /**
   * TODO: add javadoc.
   */
  public static void ecdsaSignatureVerification(byte[] encodedSignature,
      PublicKey publicKey,
      String content)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature signatureVerification = Signature.getInstance("SHA256withECDSA");
    signatureVerification.initVerify(publicKey);
    signatureVerification.update(content.getBytes(StandardCharsets.UTF_8));

    if (!signatureVerification.verify(encodedSignature)) {
      throw new SignatureException("Signature verification failed");
    }
  }
}
