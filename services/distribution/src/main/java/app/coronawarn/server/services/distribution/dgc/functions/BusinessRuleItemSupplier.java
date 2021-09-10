package app.coronawarn.server.services.distribution.dgc.functions;


import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;

public interface BusinessRuleItemSupplier<T> {


  T get() throws FetchBusinessRulesException;
}

