package app.coronawarn.server.services.distribution.dgc.client;

import static app.coronawarn.server.services.distribution.dgc.client.JsonSchemaMappingLookup.JSON_SCHEMA_PATH;
import static java.nio.charset.StandardCharsets.UTF_8;

import app.coronawarn.server.common.shared.util.ResourceSchemaClient;
import feign.FeignException;
import feign.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.stream.Collectors;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.core.io.ResourceLoader;

public class JsonSchemaDecoder extends SpringDecoder {

  private static final Logger logger = LoggerFactory.getLogger(JsonSchemaDecoder.class);
  private ResourceLoader resourceLoader;
  private JsonSchemaMappingLookup jsonSchemaMappingLookup;

  /**
   * Constructor to load the resource loader and lookup helper class.
   *
   * @param messageConverters The message converters.
   * @param customizers       The customizers.
   * @param resourceLoader    The resource loader used to load the json schema.
   */
  public JsonSchemaDecoder(final ObjectFactory<HttpMessageConverters> messageConverters,
      final ObjectProvider<HttpMessageConverterCustomizer> customizers, final ResourceLoader resourceLoader) {
    super(messageConverters, customizers);
    this.resourceLoader = resourceLoader;
    jsonSchemaMappingLookup = new JsonSchemaMappingLookup();
  }

  @Override
  public Object decode(final Response response, final Type type) throws IOException, FeignException {
    final InputStream payloadJsonInputStream = response.body().asInputStream();
    final String schemaPathToUse = getSchemaPathForRequestEndpoint(response.request().url());
    if (schemaPathToUse == null) {
      // no matching schema for this URL, so we don't need to validate
      logger.debug("No validation JSON schema defined for: {}", response.request().url());
      return super.decode(response, type);
    }
    final InputStream schemaInputStream = resourceLoader.getResource(schemaPathToUse).getInputStream();
    validateJsonAgainstSchema(payloadJsonInputStream, schemaInputStream);
    return super.decode(response, type);
  }

  /**
   * Resolve the schema that maps to the given feign client return type.
   *
   * @param requestUrl The endpoint of the request.
   * @return The schema path for the given URL.
   */
  public String getSchemaPathForRequestEndpoint(final String requestUrl) {
    return jsonSchemaMappingLookup.getSchemaPath(requestUrl);
  }

  /**
   * Validation logic to compare a json file to a schema.
   *
   * @param jsonPayloadInputStream The json payload.
   * @param schemaAsStream         The json schema.
   * @throws IOException Thrown when json could not be loaded from file.
   */
  public void validateJsonAgainstSchema(final InputStream jsonPayloadInputStream, final InputStream schemaAsStream)
      throws IOException {
    try {
      final JSONObject jsonSchema = new JSONObject(new JSONTokener(schemaAsStream));
      final SchemaClient schemaClient = new ResourceSchemaClient(resourceLoader, JSON_SCHEMA_PATH);
      final Schema schema = SchemaLoader.load(jsonSchema, schemaClient);
      // have to check manually if json is an array or an object. Limitation of the org.json library.

      // workaround for the actual json in the stream -- if we give the InputStream to the JSONTokener directly below,
      // it is saying that we don't have valid json (it says it should start with { or with [, which id DOES!)
      final String jsonPayloadString = new BufferedReader(
          new InputStreamReader(jsonPayloadInputStream, UTF_8)).lines().collect(Collectors.joining());
      try {
        final JSONObject parsedObject = new JSONObject(new JSONTokener(jsonPayloadString));
        schema.validate(parsedObject);
      } catch (final JSONException e) {
        logger.debug(e.getMessage(), e);
        final JSONArray parsedArray = new JSONArray(new JSONTokener(jsonPayloadString));
        parsedArray.forEach(schema::validate);
      }
    } catch (ValidationException | JSONException e) {
      logger.error("Json schema validation failed", e);
      throw e;
    }
  }
}
