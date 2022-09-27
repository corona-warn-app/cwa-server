package app.coronawarn.server.services.distribution.dgc.client;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonSchemaMappingLookup {

  public static final String JSON_SCHEMA_PATH = "dgc";
  public static final String CCL_JSON_SCHEMA = JSON_SCHEMA_PATH + "/ccl-configuration.json";
  public static final String DCC_VALIDATION_RULE_JSON_CLASSPATH = JSON_SCHEMA_PATH + "/dcc-validation-rule.json";
  public static final String ALLOW_LIST_JSON_CLASSPATH =
      JSON_SCHEMA_PATH + "/dcc-validation-service-allowlist-rule.json";

  static final Map<String, String> businessObjectToJsonSchema = new HashMap<>();

  private static final Logger logger = LoggerFactory.getLogger(JsonSchemaMappingLookup.class);

  /**
   * Constructor to initialize the schema mappings.
   */
  public JsonSchemaMappingLookup() {
    //initialize all mappings from URLs to the respective schema
    businessObjectToJsonSchema.put("/rules", DCC_VALIDATION_RULE_JSON_CLASSPATH);
    businessObjectToJsonSchema.put("/bnrules", DCC_VALIDATION_RULE_JSON_CLASSPATH);
    businessObjectToJsonSchema.put("/cclrules", CCL_JSON_SCHEMA);
  }

  /**
   * Returns the path to the json schema corresponding to the given URL.
   *
   * @return The path to the schema matching the request URL.
   */
  public String getSchemaPath(String requestUrl) {
    //find out if we have a known route, e.g. /rules, /bnrules, etc.
    Optional<String> match = businessObjectToJsonSchema.keySet().stream().filter(requestUrl::contains).findAny();

    if (!match.isPresent()) {
      logger.debug("Schema validation not configured for: {}", requestUrl);
      return null; // the request has not mapping, meaning there is no schema for this request URL's payload
    }

    String route = match.get();

    // find out if there are trailing elements after the base mapping (e.g. rules/<hash>). If so, we validate.
    if (requestUrl.lastIndexOf(route) >= requestUrl.lastIndexOf("/")) {
      // this means we have no hash or any other trailing path components after the route. We are not validating this.
      // The payloads we are interested in are always qualified with a hash, a country, etc.
      return null;
    }
    //since there is currently only one schema per route, we can take the route as a key for the schema
    //e.g. if we have /rules/<something>, we use the same schema, no matter what <something> is.
    return businessObjectToJsonSchema.get(route);
  }

}
