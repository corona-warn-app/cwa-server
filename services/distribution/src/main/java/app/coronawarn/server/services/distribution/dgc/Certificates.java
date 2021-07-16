package app.coronawarn.server.services.distribution.dgc;

import java.util.List;

public class Certificates {

  private List<CertificateStructure> certificates;

  public List<CertificateStructure> getCertificates() {
    return certificates;
  }

  public void setCertificates(List<CertificateStructure> certificates) {
    this.certificates = certificates;
  }
}

