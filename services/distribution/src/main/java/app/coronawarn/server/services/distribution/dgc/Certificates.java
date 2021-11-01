package app.coronawarn.server.services.distribution.dgc;

import java.util.Collection;

public class Certificates {

  private Collection<CertificateStructure> certificates;

  public Collection<CertificateStructure> getCertificates() {
    return certificates;
  }

  public void setCertificates(Collection<CertificateStructure> certificates) {
    this.certificates = certificates;
  }
}

