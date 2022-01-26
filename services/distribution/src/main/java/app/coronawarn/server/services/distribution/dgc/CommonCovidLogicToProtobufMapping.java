//package app.coronawarn.server.services.distribution.dgc;
//
//import app.coronawarn.server.common.protocols.internal.dgc.ValueSetItem;
//import app.coronawarn.server.common.protocols.internal.dgc.ValueSets;
//import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
//import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
//import app.coronawarn.server.services.distribution.dgc.exception.FetchValueSetsException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.ResourceLoader;
//import org.springframework.stereotype.Component;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Component
//public class CommonCovidLogicToProtobufMapping {
//
//  public static final String COMMON_COVID_LOGIC_DEFAULT_PATH = "ccl-configuration.json";
//
//
//  @Autowired
//  DigitalCovidCertificateClient dccClient;
//
//  @Autowired
//  DistributionServiceConfig distributionServiceConfig;
//
//  @Autowired
//  ResourceLoader resourceLoader;
//
//  private List<ValueSetMetadata> metadata;
//
//  public ValueSets constructProtobufMapping() throws FetchValueSetsException {
//
//    List<ValueSetItem> testTypeItems = toValueSetItems(readCclJson().getValueSetValues());
//
//
//    return ValueSets.newBuilder()
//        .setMa(app.coronawarn.server.common.protocols.internal.dgc.ValueSet.newBuilder().addAllItems(mahItems).build())
//        .build();
//  }
//
//
//  ValueSet readCclJson() throws FetchValueSetsException {
//    return getValueSet(COMMON_COVID_LOGIC_DEFAULT_PATH);
//  }
//
//  private List<ValueSetItem> toValueSetItems(Map<String, ValueSetObject> valueSetValues) {
//    return valueSetValues.entrySet().stream().map(
//        entry -> (ValueSetItem.newBuilder()
//            .setKey(entry.getKey())
//            .setDisplayText(entry.getValue().getDisplay())).build())
//        .collect(Collectors.toList());
//  }
//
//  private ValueSet getValueSet(String valueSetId) throws FetchValueSetsException {
//    Optional<String> hash = getValueSetHash(valueSetId);
//
//    if (hash.isPresent()) {
//      return dccClient.getValueSet(hash.get());
//    } else {
//      throw new FetchValueSetsException("Hash not found for value set id: " + valueSetId);
//    }
//  }
//
//  private Optional<String> getValueSetHash(String valueSetId) throws FetchValueSetsException {
//    if (metadata == null) {
//      // feign client either returns a non-null metadata or throw FetchValueSetsException.
//      metadata = dccClient.getValueSets();
//    }
//
//    return metadata.stream()
//        .filter(metadataItem -> metadataItem.getId().equals(valueSetId))
//        .map(ValueSetMetadata::getHash)
//        .findFirst();
//  }
//}
