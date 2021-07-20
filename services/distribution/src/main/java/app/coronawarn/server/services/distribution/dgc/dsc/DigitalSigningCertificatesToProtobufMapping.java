package app.coronawarn.server.services.distribution.dgc.dsc;


import app.coronawarn.server.common.protocols.internal.dgc.DscList;
import app.coronawarn.server.common.protocols.internal.dgc.DscListItem;
import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.dgc.CertificateStructure;
import app.coronawarn.server.services.distribution.dgc.exception.FetchDscTrustListException;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DigitalSigningCertificatesToProtobufMapping {

  @Autowired
  private DigitalSigningCertificatesClient digitalSigningCertificatesClient;

  public DscList constructProtobufMapping() throws UnableToLoadFileException, FetchDscTrustListException {
    return DscList.newBuilder().addAllCertificates(buildCertificates()).build();
  }

  private List<DscListItem> buildCertificates() throws FetchDscTrustListException {
    var certificates = digitalSigningCertificatesClient.getDscTrustList();
    List<DscListItem> dscListItems = new ArrayList<>();
    certificates.ifPresent(dscCertificates -> {
      for (CertificateStructure certs : dscCertificates.getCertificates()) {
        dscListItems.add(DscListItem.newBuilder()
            .setData(ByteString.copyFrom(Base64.getDecoder().decode(certs.getKid())))
            .setKid(ByteString.copyFrom(Base64.getDecoder().decode(certs.getRawData())))
            .build());
      }
    });
    return dscListItems;
  }
}
