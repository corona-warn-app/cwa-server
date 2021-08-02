package app.coronawarn.server.services.distribution.dgc.dsc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.dgc.dsc.decode.DscListDecoder;
import app.coronawarn.server.services.distribution.dgc.exception.DscListDecodeException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchDscTrustListException;
import java.security.SignatureException;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ProdDigitalSigningCertificateClientTest {

  @InjectMocks
  ProdDigitalSigningCertificatesClient underTest;

  @Mock
  DigitalSigningCertificatesFeignClient digitalSigningCertificatesFeignClient;

  @Mock
  DscListDecoder dscListDecoder;

  @Test
  void shouldThrowExceptionIfDscDecodeFails() throws DscListDecodeException {
    when(dscListDecoder.decode(any())).thenThrow(DscListDecodeException.class);
    when(digitalSigningCertificatesFeignClient.getDscTrustList()).thenReturn(ResponseEntity.ok().build());
    assertThatThrownBy(() -> underTest.getDscTrustList())
        .isExactlyInstanceOf(FetchDscTrustListException.class).hasCauseExactlyInstanceOf(DscListDecodeException.class);
  }

  @Test
  void shouldThrowExceptionIfTrustListFetchFails() {
    when(digitalSigningCertificatesFeignClient.getDscTrustList()).thenThrow(FeignException.class);
    assertThatThrownBy(() -> underTest.getDscTrustList())
        .isExactlyInstanceOf(FetchDscTrustListException.class).hasCauseExactlyInstanceOf(FeignException.class);
  }

}
