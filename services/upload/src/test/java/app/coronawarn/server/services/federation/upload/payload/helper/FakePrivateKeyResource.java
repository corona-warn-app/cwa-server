package app.coronawarn.server.services.federation.upload.payload.helper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class FakePrivateKeyResource {

  private static String TEST_PRIVATE_KEY = "-----BEGIN EC PRIVATE KEY-----\n"
      + "MIHcAgEBBEIBtZ6LwBSL/pyGQjk2ek28L3KML+758+F1i/mYPCEDLNrqtxby91pA\n"
      + "1VSQO4zUd1DwtwXkKp/vkjp+Ko9+IMUyQkGgBwYFK4EEACOhgYkDgYYABAHwI1C1\n"
      + "Imh5WwJingVbcS6NkNNbSXvCSJZyNjEY6y7rGbAMD9carlmuf8Z2fQpNYSazB5Z5\n"
      + "jp2Mh2EZJb+sosS8jQGDutLpHoJWH2cal6I2D19tDpH8bc1JcViyNhVdWjX9n+pi\n"
      + "7qkzqnj9Z4KJcH+luX09HAy1Vy/y99TN1EROgaJdJg==\n"
      + "-----END EC PRIVATE KEY-----";

  public static ResourceLoader makeFakeResourceLoader(String privateKey) throws IOException {
    ResourceLoader resourceLoader = mock(ResourceLoader.class);
    Resource mockedResource = mock(Resource.class);

    when(resourceLoader.getResource(anyString()))
        .thenReturn(mockedResource);
    when(mockedResource.getInputStream())
        .thenReturn(IOUtils.toInputStream(privateKey, "UTF8"));
    return resourceLoader;
  }

  public static ResourceLoader makeFakeResourceLoader() throws IOException {
    return makeFakeResourceLoader(TEST_PRIVATE_KEY);
  }

}
