package app.coronawarn.server.services.distribution.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
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
   * Deserialize json string into an object of type T. The type must also be provided to the underlying Jackson library
   * in the form of a JavaType supplied by the function parameter.
   *
   * @param jsonString           value from configuration file
   * @param typeProviderFunction type deserialization function provider
   * @return deserialized json string
   */
  public static <T> T deserializeJson(String jsonString, Function<TypeFactory, JavaType> typeProviderFunction) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper
          .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature())
          .readValue(jsonString, typeProviderFunction.apply(mapper.getTypeFactory()));
    } catch (JsonProcessingException e) {
      logger.error(e.getMessage());
      throw new IllegalStateException("Json configuration could not be deserialized");
    }
  }

  /**
   * Stringify json object T. T must be a valid Jackson object in order for properties to be correctly parsed into the
   * string. Null values will be omitted.
   * @param object Jackson object.
   * @param <T> valid object with JsonProperty notations.
   * @return String encoded JSON.
   */
  public static <T> String stringifyObject(T object) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      logger.error(e.getMessage());
      throw new IllegalStateException("Object could not be converted into JSON");
    }
  }
}
