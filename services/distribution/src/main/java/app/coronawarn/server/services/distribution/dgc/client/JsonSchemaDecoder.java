package app.coronawarn.server.services.distribution.dgc.client;

import feign.FeignException;
import feign.Response;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
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
  private final ResourceLoader resourceLoader;
  private final JsonSchemaMappingLookup jsonSchemaMappingLookup;
  private final JsonValidationService jsonValidationService;

  /**
   * Constructor to load the resource loader and lookup helper class.
   *
   * @param messageConverters The message converters.
   * @param customizers       The customizers.
   * @param resourceLoader    The resource loader used to load the json schema.
   */
  public JsonSchemaDecoder(final ObjectFactory<HttpMessageConverters> messageConverters,
      final ObjectProvider<HttpMessageConverterCustomizer> customizers, final ResourceLoader resourceLoader,
      JsonValidationService jsonValidationService) {
    super(messageConverters, customizers);
    this.resourceLoader = resourceLoader;
    jsonSchemaMappingLookup = new JsonSchemaMappingLookup();
    this.jsonValidationService = jsonValidationService;
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
    try (final InputStream schemaInputStream = resourceLoader.getResource(schemaPathToUse).getInputStream()) {
      jsonValidationService.validateJsonAgainstSchema(payloadJsonInputStream, schemaInputStream);
    }
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
}
