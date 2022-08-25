package app.coronawarn.server.services.distribution.dgc.client;


import app.coronawarn.server.common.shared.util.ResourceSchemaClient;
import feign.FeignException;
import feign.Response;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
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

  public JsonSchemaDecoder(ObjectFactory<HttpMessageConverters> messageConverters,
      ObjectProvider<HttpMessageConverterCustomizer> customizers, ResourceLoader resourceLoader) {
    this(messageConverters, customizers);
    this.resourceLoader = resourceLoader;
    jsonSchemaMappingLookup = new JsonSchemaMappingLookup();
  }

  public JsonSchemaDecoder(ObjectFactory<HttpMessageConverters> messageConverters,
      ObjectProvider<HttpMessageConverterCustomizer> customizers) {
    super(messageConverters, customizers);
  }

  @Override
  public Object decode(Response response, Type type) throws IOException, FeignException {
    InputStream businessRuleJsonInputStream = response.body().asInputStream();
    String schemaPathToUse = getSchemaPathForReturnType(type);
    InputStream schemaAsStream = resourceLoader.getResource(schemaPathToUse).getInputStream();
    validateJsonAgainstSchema(businessRuleJsonInputStream, schemaAsStream);
    return super.decode(response, type);
  }

  /**
   * Resolve the schema that maps to the given feign client return type
   *
   * @param type The return type of the call to the feign endpoint
   * @return
   */
  //TODO: write test case to test that we get the right schema for a given type
  public String getSchemaPathForReturnType(Type type) {
    return jsonSchemaMappingLookup.getSchemaPath(type);
  }

  //TODO: write test cases that asserts that:
  //1. valid json is verified as correct by the fitting schema
  //2. invalid json is rejected by the validation
  public void validateJsonAgainstSchema(InputStream businessRuleJsonInputStream, InputStream schemaAsStream)
      throws IOException {
    try {
      JSONObject jsonSchema = new JSONObject(new JSONTokener(schemaAsStream));
      SchemaClient schemaClient = new ResourceSchemaClient(resourceLoader, JsonSchemaMappingLookup.JSON_SCHEMA_PATH);
      Schema schema = SchemaLoader.load(jsonSchema, schemaClient);
      //have to check manually if json is an array or an object. Limitation of the org.json library.
      try {
        JSONObject parsedObject = new JSONObject(new JSONTokener(businessRuleJsonInputStream));
        schema.validate(parsedObject);
      } catch (JSONException e) {
        try {
          JSONArray parsedArray = new JSONArray(new JSONTokener(businessRuleJsonInputStream));
          parsedArray.forEach(object -> schema.validate(object));
        } catch (JSONException ne) {
          throw new RuntimeException("json is neither an object nor an array");
        }
      }
    } catch (ValidationException e) {
      //validation exception loggen (tiefer gehen)
      //feign exception ggf schmeissen
      logger.error("Rule json is not valid", e);
      throw e;
    }
  }
}
