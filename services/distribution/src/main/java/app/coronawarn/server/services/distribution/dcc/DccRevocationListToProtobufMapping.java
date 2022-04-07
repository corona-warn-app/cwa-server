package app.coronawarn.server.services.distribution.dcc;

import org.springframework.stereotype.Component;

@Component
public class DccRevocationListToProtobufMapping {

  private final DccRevocationClient dccRevocationClient;

  public DccRevocationListToProtobufMapping(DccRevocationClient dccRevocationClient) {
    this.dccRevocationClient = dccRevocationClient;
  }

//  public DscList constructProtobufMapping() throws UnableToLoadFileException, FetchDscTrustListException {
//    return DscList.newBuilder().addAllCertificates(buildDccList()).build();
//  }
}
