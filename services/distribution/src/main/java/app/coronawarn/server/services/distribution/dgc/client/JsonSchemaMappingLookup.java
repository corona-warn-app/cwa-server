package app.coronawarn.server.services.distribution.dgc.client;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class JsonSchemaMappingLookup {

  public static final String JSON_SCHEMA_PATH = "dgc";
  public static final String CCL_JSON_SCHEMA = JSON_SCHEMA_PATH + "/ccl-configuration.json";
  public static final String DCC_VALIDATION_RULE_JSON_CLASSPATH = JSON_SCHEMA_PATH + "/dcc-validation-rule.json";
  public static final String ALLOW_LIST_JSON_CLASSPATH =
      JSON_SCHEMA_PATH + "/dcc-validation-service-allowlist-rule.json";

  static final Map<String, String> businessObjectToJsonSchema = new HashMap<>();

  /**
   * Constructor to initialize the schema mappings.
   */
  public JsonSchemaMappingLookup() {
    //initialize all mappings from business objects to the respective schema
    //TODO: use the fully qualified names instead of objects as map keys,
    //since we can also have lists of items etc, which makes things really complicated to construct
    //TODO: switch from types to the requesst URL, since that is uniquely specific to a schema
    businessObjectToJsonSchema.put("/rules", CCL_JSON_SCHEMA);
    businessObjectToJsonSchema.put("/bnrules", DCC_VALIDATION_RULE_JSON_CLASSPATH);
    businessObjectToJsonSchema.put("/cclrules", CCL_JSON_SCHEMA);
    //businessObjectToJsonSchema.put(/allowlist, DCC_VALIDATION_RULE_JSON_CLASSPATH);
  }

  /**
   * Returns the path to the json schema corresponding to the given business class.
   *
   * @return The path to the schema matching the request URL.
   */
  public String getSchemaPath(String requestUrl) {
    //find out if we have a known route, e.g. /rules, /bnrules, etc.
    Optional<String> match = businessObjectToJsonSchema.keySet().stream().filter(s -> requestUrl.contains(s)).findAny();
    try {
      match.get();
    } catch (NoSuchElementException ex) {
      return null; // the request has not mapping, meaning there is no schema for this request URL's payload
    }
    // find out if there are trailing elements after the base mapping (e.g. rules/<hash>). If so, we validate.
    if (requestUrl.lastIndexOf(match.get()) >= requestUrl.lastIndexOf("/")) {
      //this means we have no hash or any other trailing path components after the route. We are not validating this
      //the payloads we are interested in are always qualified with a hash, a country, etc.
      return null;
    }
    //since there is currently only one schema per route, we can take the route as a key for the schema
    //e.g. if we have /rules/<something>, we use the same schema, no matter what <something> is.
    return businessObjectToJsonSchema.get(match.get());
  }

}
