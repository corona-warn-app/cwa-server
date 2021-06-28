package app.coronawarn.server.services.distribution.dgc;

import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.springframework.stereotype.Component;

@Component
public class DigitalGreenCertificateToCborMapping {

  private DigitalCovidCertificateClient digitalCovidCertificateClient;

  public DigitalGreenCertificateToCborMapping(DigitalCovidCertificateClient digitalCovidCertificateClient) {
    this.digitalCovidCertificateClient = digitalCovidCertificateClient;
  }

  /**
   * TODO: write javadoc.
   */
  public byte[] constructCountryList() {
    ObjectMapper cborMapper = new CBORMapper();
    try {
      return cborMapper.writeValueAsBytes(digitalCovidCertificateClient.getCountryList());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * TODO: write javadoc.
   */
  public byte[] constructRules() {
    ObjectMapper cborMapper = new CBORMapper();
    try {
      return cborMapper.writeValueAsBytes(digitalCovidCertificateClient.getRules());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    return null;
  }

}
