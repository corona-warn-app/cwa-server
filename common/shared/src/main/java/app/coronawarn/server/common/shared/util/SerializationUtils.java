package app.coronawarn.server.common.shared.util;

import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.upokecenter.cbor.CBORObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ObjectUtils;

public final class SerializationUtils {

  private static final Logger logger = LoggerFactory.getLogger(SerializationUtils.class);

  /**
   * Deserialize json string into an object of type T. The type must also be provided to the underlying Jackson library
   * in the form of a JavaType supplied by the function parameter.
   *
   * @param jsonString           value from configuration file
   * @param typeProviderFunction type deserialization function provider
   * @param <T>                  generic type
   * @return deserialized json string
   */
  public static <T> T deserializeJson(final String jsonString,
      final Function<TypeFactory, JavaType> typeProviderFunction) {
    final ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature()).readValue(jsonString,
          typeProviderFunction.apply(mapper.getTypeFactory()));
    } catch (final JsonProcessingException e) {
      throw new IllegalStateException("Json configuration could not be deserialized", e);
    }
  }

  /**
   * Parse json from stream instead from string.
   *
   * @param jsonStream           stream to read json from
   * @param typeProviderFunction type deserialization function provider
   * @return deserialized json as pojo
   * @throws IOException coming from {@link ObjectMapper#readValue(InputStream, JavaType)}.
   */
  public static <T> T deserializeJson(final InputStream jsonStream,
      final Function<TypeFactory, JavaType> typeProviderFunction) throws IOException {
    final ObjectMapper mapper = JsonMapper.builder()
        .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature())
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build();
    return mapper.readValue(jsonStream, typeProviderFunction.apply(mapper.getTypeFactory()));
  }

  /**
   * Stringify json object T. T must be a valid Jackson object in order for properties to be correctly parsed into the
   * string. Null values will be omitted.
   *
   * @param object Jackson object.
   * @param <T>    valid object with JsonProperty notations.
   * @return String encoded JSON.
   */
  public static <T> String stringifyObject(final T object) {
    final ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(object);
    } catch (final JsonProcessingException e) {
      throw new IllegalStateException("Object could not be converted into JSON", e);
    }
  }

  public static <T> Optional<T> deserializeJsonToSimpleType(InputStream jsonStream, Class<T> rawType)
      throws IOException {
    return Optional.of(deserializeJson(jsonStream,
        typeFactory -> typeFactory.constructSimpleType(rawType, new JavaType[0])));
  }

  /**
   * Extracts JSONObject from COSE byte sequence.
   *
   * @param byteSequence payload to be parsed
   * @return parsed payload as JSON object
   * @throws IOException    if parser cannot be created
   * @throws ParseException if byte sequence cannot be parsed
   */
  public static Map<byte[], List<byte[]>> jsonExtractCosePayload(byte[] byteSequence)
      throws IOException, ParseException {

    CBORObject cborObject = CBORObject.DecodeFromBytes(byteSequence);
    CBORObject payload = CBORObject.DecodeFromBytes(cborObject.get(2).GetByteString());

    Map<byte[], List<byte[]>> payloadEntries = new HashMap<>();
    payload.getKeys().forEach(key -> {
      List<byte[]> values = payload.get(key).getValues().stream()
          .map(CBORObject::GetByteString).collect(Collectors.toList());
      payloadEntries.put(key.GetByteString(), values);
    });
    return payloadEntries;
  }

  /**
   * Reads and converts a JSON object from a classpath file or (if not found), it returns a default.
   *
   * @param resourceLoader - resource loader.
   * @param path           - JSON path.
   * @param defaultPath    - default JSON path.
   * @param rawType        - type to convert to.
   * @param <T>            - type of the method.
   * @return - converted JSON to raw type instance.
   * @throws UnableToLoadFileException - if default JSON is not found.
   */
  public static <T> Optional<T> readConfiguredJsonOrDefault(ResourceLoader resourceLoader,
      String path,
      String defaultPath,
      Class<T> rawType) {
    if (!ObjectUtils.isEmpty(path)) {
      try (InputStream jsonStream = resourceLoader.getResource(path).getInputStream()) {
        logger.debug("Loading JSON from {}.", path);
        return deserializeJsonToSimpleType(jsonStream, rawType);
      } catch (IOException e) {
        logger.error("Error reading {} from json {}.", rawType.getSimpleName(), path, e);
      }
    }
    try (InputStream jsonStream = resourceLoader.getResource(defaultPath).getInputStream()) {
      // fallback to default
      logger.debug("JSON to load was empty or invalid, falling back to loading from {}.", defaultPath);
      return deserializeJsonToSimpleType(jsonStream, rawType);
    } catch (IOException e) {
      logger.error("We could not load the default {}. This shouldn't happen!", defaultPath, e);
      return Optional.empty();
    }
  }

  /**
   * Encodes an object to CBOR.
   *
   * @param object - object to be encoded
   * @return - CBOR encoded byte array
   * @throws JsonProcessingException - if JSON processing of the object fails.
   */
  public static byte[] cborEncode(Object object) throws JsonProcessingException {
    ObjectMapper cborMapper = new CBORMapper();
    return cborMapper.writeValueAsBytes(object);
  }

  private SerializationUtils() {
  }

}
