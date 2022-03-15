package app.coronawarn.server.services.distribution.dgc.functions;


import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;

public interface BusinessRuleSupplier<R, A, B> {


  R get(A a, B b) throws FetchBusinessRulesException;
}
