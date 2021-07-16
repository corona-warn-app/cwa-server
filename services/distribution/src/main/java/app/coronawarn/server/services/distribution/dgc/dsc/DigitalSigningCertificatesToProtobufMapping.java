package app.coronawarn.server.services.distribution.dgc.dsc;


import app.coronawarn.server.common.protocols.internal.dgc.DscList;
import app.coronawarn.server.common.protocols.internal.dgc.DscListItem;
import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.dgc.CertificateStructure;
import app.coronawarn.server.services.distribution.dgc.exception.FetchDscTrustListException;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DigitalSigningCertificatesToProtobufMapping {

  private final DigitalSigningCertificatesClient digitalSigningCertificatesClient;

  public DigitalSigningCertificatesToProtobufMapping(
      DigitalSigningCertificatesClient digitalSigningCertificatesClient) {
    this.digitalSigningCertificatesClient = digitalSigningCertificatesClient;
  }

  public DscList constructProtobufMapping() throws UnableToLoadFileException, FetchDscTrustListException {
    return DscList.newBuilder().addAllCertificates(buildCertificates()).build();
  }

  private List<DscListItem> buildCertificates() throws FetchDscTrustListException {
    var certificates = digitalSigningCertificatesClient.getDscTrustList();
    List<DscListItem> dscListItems = new ArrayList<>();
    certificates.ifPresent(dscCertificates -> {
      for (CertificateStructure certs : dscCertificates.getCertificates()) {
        dscListItems.add(DscListItem.newBuilder()
            .setData(ByteString.EMPTY)
            .setKid(ByteString.EMPTY).build());
      }
    });
    return dscListItems;
  }
}
