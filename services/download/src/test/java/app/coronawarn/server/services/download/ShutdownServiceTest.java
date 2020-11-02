package app.coronawarn.server.services.download;

import static org.mockito.Mockito.mock;

import java.security.Permission;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext
class ShutdownServiceTest {

  @Autowired
  private ShutdownService underTest;


  @BeforeAll
  public static void setup() {
    System.setSecurityManager(new SecurityManager() {
      @Override
      public void checkPermission(Permission perm) {
      }

      @Override
      public void checkPermission(Permission perm, Object context) {
      }

      @Override
      public void checkExit(int status) {
        super.checkExit(status);
        throw new SecurityException("Prevented System Exit");
      }
    });
  }

  @AfterAll
  public static void tearDown() {
    System.setSecurityManager(null);
  }

  @Test
  void testShutdownApplication() {
    try {
      underTest.shutdownApplication(mock(ApplicationContext.class));
    } catch (SecurityException e) {
      Assertions.assertThat(e).hasMessage("Prevented System Exit");
    }
  }

}
