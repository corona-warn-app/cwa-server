package app.coronawarn.server.services.distribution.statistics.keyfigurecard;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.EMPTY_CARD;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.INCIDENCE_CARD_ID;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.INFECTIONS_CARD_ID;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.KEY_SUBMISSION_CARD_ID;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.REPRODUCTION_NUMBER_CARD;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.EmptyCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.HeaderCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.IncidenceCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.InfectionsCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.KeySubmissionCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.ReproductionNumberCardFactory;
import org.springframework.stereotype.Component;

@Component
public class KeyFigureCardFactory {

  private final ValueTrendCalculator valueTrendCalculator;

  public KeyFigureCardFactory(DistributionServiceConfig config) {
    this.valueTrendCalculator = new ValueTrendCalculator(config.getStatistics().getTrendCalculationThreshold());
  }

  private HeaderCardFactory getFactoryPerCardId(int cardId) {
    switch (cardId) {
      case INFECTIONS_CARD_ID:
        return new InfectionsCardFactory(this.valueTrendCalculator);
      case INCIDENCE_CARD_ID:
        return new IncidenceCardFactory(this.valueTrendCalculator);
      case KEY_SUBMISSION_CARD_ID:
        return new KeySubmissionCardFactory(this.valueTrendCalculator);
      case REPRODUCTION_NUMBER_CARD:
        return new ReproductionNumberCardFactory(this.valueTrendCalculator);
      case EMPTY_CARD:
      default:
        return new EmptyCardFactory(this.valueTrendCalculator);
    }
  }

  public KeyFigureCard createKeyFigureCard(StatisticsJsonStringObject stats, Integer cardId) {
    return this.getFactoryPerCardId(cardId).makeKeyFigureCard(stats);
  }
}
