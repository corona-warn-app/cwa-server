package app.coronawarn.server.services.distribution.assembly.component;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommonCovidLogicArchiveBuilderTest {


  public static final String EXPORT_BINARY_FILENAME = "export-test.bin";
  public static final String CCL_DIRECTORY_NAME = "ccl";

  @Mock
  TestDigitalCovidCertificateClient testDigitalCovidCertificateClient;

  @InjectMocks
  CommonCovidLogicArchiveBuilder commonCovidLogicArchiveBuilder;

  @Test
  void fetchingBusinessRulesItemsShouldThrowExceptionWhenClientThrowsException()
      throws FetchBusinessRulesException {
    when(testDigitalCovidCertificateClient.getCommonCovidLogicRules()).thenThrow(FetchBusinessRulesException.class);

    assertThrows(FetchBusinessRulesException.class, () -> commonCovidLogicArchiveBuilder
        .setRuleType(RuleType.COMMON_COVID_LOGIC)
        .setDirectoryName(CCL_DIRECTORY_NAME)
        .setExportBinaryFilename(EXPORT_BINARY_FILENAME)
        .setBusinessRuleItemSupplier(testDigitalCovidCertificateClient::getCommonCovidLogicRules)
        .setBusinessRuleSupplier(testDigitalCovidCertificateClient::getCommonCovidLogicRuleByHash)
        .build());
  }
}
