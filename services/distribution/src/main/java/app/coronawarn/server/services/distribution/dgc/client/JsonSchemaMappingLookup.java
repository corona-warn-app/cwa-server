package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import java.lang.reflect.Type;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class JsonSchemaMappingLookup {

  public static final String JSON_SCHEMA_PATH = "dgc";
  public static final String CCL_JSON_SCHEMA = JSON_SCHEMA_PATH + "/ccl-configuration.json";

  final static Map<Type, String> businessObjectToJsonSchema = new HashMap<>();
  //use in case schema location depends on business object class AND rule type
  final static Map<Entry<Class, RuleType>, String> classAndRuletypeToSchemaClientPath = new HashMap<>();

  public JsonSchemaMappingLookup() {
    //initialize all mappings from business objects to the respective schema
    businessObjectToJsonSchema.put(BusinessRuleItem.class, CCL_JSON_SCHEMA);
    businessObjectToJsonSchema.put(BusinessRule.class, CCL_JSON_SCHEMA);
  }

  /**
   * Returns the path to the json schema corresponding to the given business class
   *
   * @return
   */
  public String getSchemaPath(Type businessClass) {
    return businessObjectToJsonSchema.get(businessClass);
  }

  //Only needed if we actually need to consider the rule type in addition to the business class to find the schema
  public String getSchemaClientPath(Class clazz, RuleType ruleType) {
    return classAndRuletypeToSchemaClientPath.get(new SimpleEntry<>(clazz, ruleType));
  }


}
