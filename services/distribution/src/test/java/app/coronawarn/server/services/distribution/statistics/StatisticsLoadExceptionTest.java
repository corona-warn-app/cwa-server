package app.coronawarn.server.services.distribution.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.service.StatisticsDownloadService;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.exceptions.BucketNotFoundException;
import app.coronawarn.server.services.distribution.statistics.exceptions.ConnectionException;
import app.coronawarn.server.services.distribution.statistics.exceptions.FilePathNotFoundException;
import app.coronawarn.server.services.distribution.statistics.file.JsonFileLoader;
import app.coronawarn.server.services.distribution.statistics.file.MockStatisticJsonFileLoader;
import app.coronawarn.server.services.distribution.statistics.file.StatisticJsonFileLoader;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardFactory;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = {DistributionServiceConfig.class})
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"local-json-stats"})
@ContextConfiguration(classes = {StatisticsJsonToProtobufTest.class,
    StatisticsToProtobufMapping.class, KeyFigureCardFactory.class,
    MockStatisticJsonFileLoader.class
}, initializers = ConfigDataApplicationContextInitializer.class)
class StatisticsLoadExceptionTest {

  @SpyBean
  StatisticJsonFileLoader loader;

  @Autowired
  DistributionServiceConfig serviceConfig;

  @Autowired
  KeyFigureCardFactory keyFigureCardFactory;

  @MockBean
  StatisticsDownloadService statisticsDownloadService;

  @ParameterizedTest
  @ValueSource(classes = {
      BucketNotFoundException.class,
      FilePathNotFoundException.class,
      ConnectionException.class
  })
  void shouldNotGenerateProtobufFileIfException(Class<RuntimeException> exception) {
    when(loader.getFile()).thenThrow(exception);
    when(statisticsDownloadService.getMostRecentDownload()).thenReturn(Optional.empty());
    var statisticsToProtobufMapping = new StatisticsToProtobufMapping(serviceConfig, keyFigureCardFactory, loader, statisticsDownloadService);
    assertThat(statisticsToProtobufMapping.constructProtobufStatistics().getKeyFigureCardsCount())
        .isZero();
  }
}
