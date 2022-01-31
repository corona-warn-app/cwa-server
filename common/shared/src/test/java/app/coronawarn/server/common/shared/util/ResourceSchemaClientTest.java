package app.coronawarn.server.common.shared.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.FileNotFoundException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ResourceSchemaClientTest {


  @Autowired
  private ResourceLoader resourceLoader;

  private final String path = "dgc/";

  @Test
  void doesTakeTheCorrectResourceFromPath() {

    String correctFile = "ccl-configuration.json";
    ResourceSchemaClient resourceSchemaClient = new ResourceSchemaClient(resourceLoader, "dgc");
    InputStream inputStream = resourceSchemaClient.get(path + correctFile);

    assertThat(inputStream).isNotEmpty();
  }

  @Test
  void throwsExceptionWhenTheFileNameIsIncorrect() {

    String incorrectFile = "ccl-configuration-sample.json";

    assertThrows(FileNotFoundException.class,
        () -> resourceLoader.getResource(path + incorrectFile).getInputStream());
  }
}
