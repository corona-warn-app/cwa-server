package app.coronawarn.server.services.distribution.dgc.client;

import static app.coronawarn.server.services.distribution.dgc.client.JsonSchemaMappingLookup.JSON_SCHEMA_PATH;
import static java.nio.charset.StandardCharsets.UTF_8;

import app.coronawarn.server.common.shared.util.ResourceSchemaClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.springframework.core.io.ResourceLoader;

public class JsonValidationService {

  private static final Logger logger = LoggerFactory.getLogger(JsonValidationService.class);
  ResourceLoader resourceLoader;
  JsonSchemaMappingLookup jsonSchemaMappingLookup;

  public JsonValidationService(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
    jsonSchemaMappingLookup = new JsonSchemaMappingLookup();
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
      try {
        final JSONObject parsedObject = new JSONObject(new JSONTokener(new BufferedReader(
            new InputStreamReader(jsonPayloadInputStream, UTF_8))));
        schema.validate(parsedObject);
      } catch (final JSONException e) {
        logger.debug(e.getMessage(), e);
        final JSONArray parsedArray = new JSONArray(new JSONTokener(new BufferedReader(
            new InputStreamReader(jsonPayloadInputStream, UTF_8))));
        parsedArray.forEach(schema::validate);
      }
    } catch (ValidationException | JSONException e) {
      logger.error("Json schema validation failed", e);
      throw e;
    }
  }

}
