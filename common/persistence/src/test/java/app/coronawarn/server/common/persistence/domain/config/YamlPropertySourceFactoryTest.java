package app.coronawarn.server.common.persistence.domain.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;

@DataJdbcTest
@ExtendWith(MockitoExtension.class)
class YamlPropertySourceFactoryTest {

  @Autowired
  private TekFieldDerivations tekDerivations;

  @Autowired
  private YamlPropertySourceFactory propertySourceFactory;

  @Mock
  private YamlPropertiesFactoryBean factoryBean;

  @Mock
  private Resource resource;

  private EncodedResource encodedResource;

  @BeforeEach
  void setUp() {
    encodedResource = new EncodedResource(resource);
  }

  @Test
  void test() {
    assertNotNull(tekDerivations);
    assertNotNull(propertySourceFactory);
    assertThat(tekDerivations.getDaysSinceSymptomsFromTransmissionRiskLevel()).isNotEmpty();
    assertThat(tekDerivations.getTransmissionRiskLevelFromDaysSinceSymptoms()).isNotEmpty();
  }

  @Test
  void testCreatePropertySourceWithNullFactoryObjectThrowsException() {
    YamlPropertySourceFactory factory = new YamlPropertySourceFactory(factoryBean);
    NullPointerException exception = Assertions
        .assertThrows(NullPointerException.class,
            () -> factory.createPropertySource("test", encodedResource));
    assertEquals("Properties must not be null", exception.getMessage());
  }

  @Test
  void testCreatePropertySourceWithNullEncodedResourceFileNameThrowsException() {
    when(factoryBean.getObject()).thenReturn(new Properties());
    YamlPropertySourceFactory factory = new YamlPropertySourceFactory(factoryBean);
    NullPointerException exception = Assertions
        .assertThrows(NullPointerException.class,
            () -> factory.createPropertySource("test", encodedResource));
    assertEquals("File name must not be null", exception.getMessage());
  }

  @Test
  void testCreatePropertySourceShouldBeSuccessfully() {
    when(factoryBean.getObject()).thenReturn(new Properties());
    when(resource.getFilename()).thenReturn("filename");
    YamlPropertySourceFactory factory = new YamlPropertySourceFactory(factoryBean);
    PropertySource<?> propertySource = factory.createPropertySource("test", encodedResource);
    Assertions.assertEquals("filename", propertySource.getName());
  }
}
