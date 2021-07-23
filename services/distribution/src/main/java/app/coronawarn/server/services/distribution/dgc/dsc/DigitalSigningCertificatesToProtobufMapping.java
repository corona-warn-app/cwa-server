package app.coronawarn.server.services.distribution.dgc.dsc;

import static app.coronawarn.server.common.shared.util.SecurityUtils.base64decode;

import app.coronawarn.server.common.protocols.internal.dgc.DscList;
import app.coronawarn.server.common.protocols.internal.dgc.DscListItem;
import app.coronawarn.server.common.shared.exception.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.dgc.CertificateStructure;
import app.coronawarn.server.services.distribution.dgc.exception.FetchDscTrustListException;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
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
            .setData(ByteString.copyFrom(base64decode(certs.getRawData())))
            .setKid(ByteString.copyFrom(base64decode(certs.getKid())))
            .build());
      }
    });
    return dscListItems;
  }
}
