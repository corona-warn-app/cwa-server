package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import app.coronawarn.server.services.distribution.dgc.ValueSetMetadata;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchValueSetsException;
import java.util.List;

/**
 * This is a wrapper interface retrieving Digital Covid Certificate data. Used to make HTTP request to Digital Covid
 * Certificate server. Used to retrieve mock sample data from classpath.
 */
public interface DigitalCovidCertificateClient {

  List<String> getCountryList() throws FetchBusinessRulesException;

  List<ValueSetMetadata> getValueSets() throws FetchValueSetsException;

  ValueSet getValueSet(String hash) throws FetchValueSetsException;

  List<BusinessRuleItem> getRules() throws FetchBusinessRulesException;

  List<BusinessRuleItem> getBoosterNotificationRules() throws FetchBusinessRulesException;

  BusinessRule getCountryRuleByHash(String country, String hash) throws FetchBusinessRulesException;

  BusinessRule getBoosterNotificationRuleByHash(String country, String hash) throws FetchBusinessRulesException;
}
