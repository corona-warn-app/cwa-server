package app.coronawarn.server.common.shared.util;

import static app.coronawarn.server.common.shared.util.HashUtils.Algorithms.EC;
import static app.coronawarn.server.common.shared.util.HashUtils.Algorithms.SHA_ECDSA;
import static java.util.Arrays.copyOfRange;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequence;

public class SecurityUtils {

  /**
   * Static delegator for {@link Base64#getDecoder()} / {@link Decoder#decode(byte[])}.
   *
   * @param in String to be decoded
   * @return decoded byte array
   */
  public static byte[] base64decode(final String in) {
    return Base64.getDecoder().decode(in);
  }

  /**
   * Verify an ECDSA signature by using the provided content and public key.
   *
   * @param encodedSignature - ecdsa ready signature.
   * @param publicKey        - public key.
   * @param content          - content to verify.
   * @throws NoSuchAlgorithmException - thrown if signature verification algorithm is not available.
   * @throws InvalidKeyException      - thrown if the public key is invalid.
   * @throws SignatureException       - thrown if the signature verification fails.
   */
  public static void ecdsaSignatureVerification(final byte[] encodedSignature, final PublicKey publicKey,
      final byte[] content) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    final Signature signatureVerification = Signature.getInstance(SHA_ECDSA.getName());
    signatureVerification.initVerify(publicKey);
    signatureVerification.update(content);

    if (!signatureVerification.verify(encodedSignature)) {
      throw new SignatureException("Signature verification " + signatureVerification.getAlgorithm()
              + " for encoded signature " + Base64.getEncoder().encodeToString(encodedSignature)
              + " with publicKey " + publicKey
              + " and content " + Base64.getEncoder().encodeToString(content)
              + " failed.");
    }
  }

  /**
   * Process signature for elliptic curve encoding. Signature is a Base64 encoded String. Split signature into two
   * halves and use bountycastle to transform it into a DER Sequence.
   *
   * @param signature - base64 signature.
   * @return - ecdsa ready to verify signature.
   * @throws IOException - thrown by DER sequence encoding method.
   */
  public static byte[] getEcdsaEncodeFromSignature(final byte[] signature) throws IOException {
    final byte[] left = copyOfRange(signature, 0, signature.length / 2);
    final byte[] right = copyOfRange(signature, signature.length / 2, signature.length);

    final ASN1EncodableVector encodableVector = new ASN1EncodableVector();
    encodableVector.add(new ASN1Integer(new BigInteger(1, left)));
    encodableVector.add(new ASN1Integer(new BigInteger(1, right)));

    final DERSequence derSequence = new DERSequence(encodableVector);
    return derSequence.getEncoded();
  }

  /**
   * Transforms a String public key into a Java security public key.
   *
   * @param publicKey - String public Key.
   * @return - Java public key of the identity whose signature is going to be verified.
   * @throws NoSuchAlgorithmException - thrown if key factory algorithm is not available.
   * @throws InvalidKeySpecException  - thrown if public key is not valid.
   */
  public static PublicKey getPublicKeyFromString(final String publicKey)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    final X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(base64decode(publicKey));
    return KeyFactory.getInstance(EC.getName()).generatePublic(x509EncodedKeySpec);
  }

  private SecurityUtils() {
  }
}
