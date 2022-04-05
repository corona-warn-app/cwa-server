package app.coronawarn.server.common.shared.util;

import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.Function;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.simple.parser.JSONParser;
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

  public static org.json.simple.JSONObject jsonExtractCosePayload(byte[] byteSequence) throws IOException, ParseException {
    CBORFactory cborFactory = new CBORFactory();
    CBORParser cborParser = cborFactory.createParser(byteSequence);
    JsonFactory jsonFactory = new JsonFactory();
    StringWriter stringWriter = new StringWriter();
    JsonGenerator jsonGenerator = jsonFactory.createGenerator(stringWriter);
    StringWriter stringWriter2 = new StringWriter();
    JsonGenerator jsonGenerator2 = jsonFactory.createGenerator(stringWriter2);
    JSONParser parser = new JSONParser();

    int i = 0;
    while (cborParser.nextToken() != null) {
      if (JsonTokenId.ID_EMBEDDED_OBJECT == cborParser.getCurrentToken().id() && ++i == 2) {
        byte[] obj = (byte[]) cborParser.getEmbeddedObject();
        CBORParser payload = cborFactory.createParser(obj);
        while (payload.nextToken() != null) {
          jsonGenerator2.copyCurrentEvent(payload);
        }
        i++;
      }
      jsonGenerator.copyCurrentEvent(cborParser);
    }
    jsonGenerator.flush();
    jsonGenerator2.flush();

    return (org.json.simple.JSONObject) parser.parse(stringWriter2.toString());
  }

  /**
   * Reads and convers a JSON object from a classpath file or if it does not find it returns a default.
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

  /**
   * Validates an object (JSON) based on a provided schema containing validation rules.
   *
   * @param validateObject       - object to be validated
   * @param schemaValidationJson - validation schema
   * @throws JsonProcessingException - if object to be validated fails on JSON processing
   * @throws ValidationException     - if the validation of the object based on validation schema fails.
   */
  public static void validateJsonSchema(Object validateObject, InputStream schemaValidationJson,
      final SchemaClient schemaClient)
      throws JsonProcessingException, ValidationException {
    ObjectMapper objectMapper = new ObjectMapper();
    JSONObject jsonSchema = new JSONObject(new JSONTokener(schemaValidationJson));
    String businessRuleJson = objectMapper.writeValueAsString(validateObject);

    JSONObject jsonSubject = new JSONObject(businessRuleJson);
    Schema schema = schemaClient == null ? SchemaLoader.load(jsonSchema) : SchemaLoader.load(jsonSchema, schemaClient);
    schema.validate(jsonSubject);
  }

  public static void validateJsonSchema(Object validateObject, InputStream schemaValidationJson)
      throws JsonProcessingException, ValidationException {
    validateJsonSchema(validateObject, schemaValidationJson, null);
  }

  private SerializationUtils() {
  }

}
