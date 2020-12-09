package app.coronawarn.server.services.distribution.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.file.JsonFileLoader;
import app.coronawarn.server.services.distribution.statistics.file.LocalStatisticJsonFileLoader;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = {DistributionServiceConfig.class})
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"local-json-stats"})
@ContextConfiguration(classes = {StatisticsJsonToProtobufTest.class,
    StatisticsToProtobufMapping.class, KeyFigureCardFactory.class,
    LocalStatisticJsonFileLoader.class
}, initializers = ConfigFileApplicationContextInitializer.class)
public class StatisticsLoadExceptionTest {

  @SpyBean
  JsonFileLoader loader;

  @Autowired
  DistributionServiceConfig serviceConfig;

  @Autowired
  KeyFigureCardFactory keyFigureCardFactory;

  @Test
  void testGenerateEmptyStatisticsIfConnectionException() {
    when(loader.getContent()).thenThrow(ExhaustedRetryException.class);
    var statisticsToProtobufMapping = new StatisticsToProtobufMapping(serviceConfig, keyFigureCardFactory, loader);
    assertThat(statisticsToProtobufMapping.constructProtobufStatistics().getKeyFigureCardsCount())
        .isEqualTo(0);
  }
}
