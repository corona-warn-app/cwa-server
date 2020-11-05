package app.coronawarn.server.services.distribution.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SerializationUtils {

  private static final Logger logger = LoggerFactory.getLogger(SerializationUtils.class);

  private SerializationUtils() {
  }

  /**
   * Deserialize json string into an object of type T. The type must also be provided to the underlying Jackson
   * library in the form of a JavaType supplied by the function parameter.
   *
   * @param jsonString           value from configuration file
   * @param typeProviderFunction type deserialization function provider
   * @return deserialized json string
   */
  public static <T> T deserializeJson(String jsonString, Function<TypeFactory, JavaType> typeProviderFunction) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(jsonString, typeProviderFunction.apply(mapper.getTypeFactory()));
    } catch (JsonProcessingException e) {
      logger.error(e.getMessage());
      throw new IllegalStateException("Json configuration could not be deserialized");
    }
  }
}
