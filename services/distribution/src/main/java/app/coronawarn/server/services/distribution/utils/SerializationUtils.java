package app.coronawarn.server.services.distribution.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Function;

public final class SerializationUtils {
  private static final Logger logger = LoggerFactory.getLogger(SerializationUtils.class);

  private SerializationUtils() {
  }

  public static Object deserializeJson(String jsonString, Function<TypeFactory, JavaType> typeProviderFunction) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(jsonString, typeProviderFunction.apply(mapper.getTypeFactory()));
    } catch (JsonProcessingException e) {
      logger.error(e.getMessage());
      throw new IllegalStateException("Json configuration could not be deserialized");
    }
  }
}
