package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class JsonSchemaMappingLookup {

  public static final String JSON_SCHEMA_PATH = "dgc";
  public static final String CCL_JSON_SCHEMA = JSON_SCHEMA_PATH + "/ccl-configuration.json";
  public static final String DCC_VALIDATION_RULE_JSON_CLASSPATH =
      JSON_SCHEMA_PATH + "/dcc-validation-service-allowlist-rule.json";

  final static Map<String, String> businessObjectToJsonSchema = new HashMap<>();
  //use in case schema location depends on business object class AND rule type
  final static Map<Entry<Class, RuleType>, String> classAndRuletypeToSchemaClientPath = new HashMap<>();

  public JsonSchemaMappingLookup() {
    //initialize all mappings from business objects to the respective schema
    //TODO: use the fully qualified names instead of objects as map keys,
    //since we can also have lists of items etc, which makes things really complicated to construct
    //TODO: switch from types to the requesst URL, since that is uniquely specific to a schema
    businessObjectToJsonSchema.put("/countrylist", CCL_JSON_SCHEMA);
    businessObjectToJsonSchema.put("/valuesets", CCL_JSON_SCHEMA);
    businessObjectToJsonSchema.put("/rules", CCL_JSON_SCHEMA);
    businessObjectToJsonSchema.put("/bnrules", CCL_JSON_SCHEMA);
    businessObjectToJsonSchema.put("/cclrules", CCL_JSON_SCHEMA);

    //businessObjectToJsonSchema.put(AllowList.class, DCC_VALIDATION_RULE_JSON_CLASSPATH);
    //TODO: map the revocation feign provider as well?
  }

  /**
   * Returns the path to the json schema corresponding to the given business class
   *
   * @return
   */
  public String getSchemaPath(String requestUrl) {
    String route = requestUrl.substring(requestUrl.lastIndexOf("/"), requestUrl.length());
    return businessObjectToJsonSchema.get(route);
  }

  //Only needed if we actually need to consider the rule type in addition to the business class to find the schema
  public String getSchemaClientPath(Class clazz, RuleType ruleType) {
    return classAndRuletypeToSchemaClientPath.get(new SimpleEntry<>(clazz, ruleType));
  }
}
