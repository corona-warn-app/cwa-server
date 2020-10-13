

package app.coronawarn.server.services.federation.upload.utils;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SecretResourceMockData {

  public static String TEST_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n"
      + "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgjPus/CYkGPQhgBtC\n"
      + "MtG+vC2BQkMDkbcswEP182fFJ7ehRANCAATWXHdqcXz9bNmoxoLlCK8wHpy/ws28\n"
      + "CyvzB3xgOEeBBbuGKFPBcu5PRxN1mLe3jU0K2bvcLDcvbuP5/Qfq25Z3\n"
      + "-----END PRIVATE KEY-----";

  public static String TEST_PUBLIC_KEY = "-----BEGIN CERTIFICATE-----\n"
      + "MIIBtjCCAVygAwIBAgIJAKHUcVXhuN3dMAoGCCqGSM49BAMCMDkxFDASBgNVBAMM\n"
      + "C0ZHUyBHZXJtYW55MRQwEgYDVQQLDAtDb3JvbmEtVGVhbTELMAkGA1UEBhMCREUw\n"
      + "HhcNMjAwODI4MTIzODEwWhcNMjEwODI4MTIzODEwWjA5MRQwEgYDVQQDDAtGR1Mg\n"
      + "R2VybWFueTEUMBIGA1UECwwLQ29yb25hLVRlYW0xCzAJBgNVBAYTAkRFMFkwEwYH\n"
      + "KoZIzj0CAQYIKoZIzj0DAQcDQgAE1lx3anF8/WzZqMaC5QivMB6cv8LNvAsr8wd8\n"
      + "YDhHgQW7hihTwXLuT0cTdZi3t41NCtm73Cw3L27j+f0H6tuWd6NNMEswMQYDVR0l\n"
      + "BCowKAYIKwYBBQUHAwEGCCsGAQUFBwMCBggrBgEFBQcDAwYIKwYBBQUHAwQwCQYD\n"
      + "VR0TBAIwADALBgNVHQ8EBAMCBeAwCgYIKoZIzj0EAwIDSAAwRQIhAOtEEWyA4eG2\n"
      + "GopTSvi+yXhP/EuyYT4dDCoCdCVVQurvAiA/t1YHxWtVQD58E1jIaOK6+mkrECxx\n"
      + "vTq/go9KgBdn3g==\n"
      + "-----END CERTIFICATE-----\n";

  public static ResourceLoader makeFakeResourceLoader(String privateKey, String publicKey) throws IOException {
    ResourceLoader resourceLoader = mock(ResourceLoader.class);
    Resource privateKeyResource = mock(Resource.class);
    when(privateKeyResource.getInputStream())
        .thenReturn(IOUtils.toInputStream(privateKey, "UTF8"));

    Resource publicKeyResource = mock(Resource.class);
    when(publicKeyResource.getInputStream())
        .thenReturn(IOUtils.toInputStream(publicKey, "UTF8"));

    when(resourceLoader.getResource("classpath:testprivatekey.pem"))
        .thenReturn(privateKeyResource);
    when(resourceLoader.getResource("classpath:testpublickey.pem"))
        .thenReturn(publicKeyResource);

    return resourceLoader;
  }

  public static ResourceLoader makeFakeResourceLoader() throws IOException {
    return makeFakeResourceLoader(TEST_PRIVATE_KEY, TEST_PUBLIC_KEY);
  }

}
