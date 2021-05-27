package app.coronawarn.server.services.distribution.statistics.keyfigurecard;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.FIRST_VACCINATION_CARD;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.FULLY_VACCINATED_CARD;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.INCIDENCE_CARD_ID;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.INFECTIONS_CARD_ID;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.KEY_SUBMISSION_CARD_ID;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.REPRODUCTION_NUMBER_CARD;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.VACCINATION_DOSES_CARD;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.EmptyCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.FirstVaccinationCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.FullyVaccinatedCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.HeaderCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.IncidenceCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.InfectionsCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.KeySubmissionCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.ReproductionNumberCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.VaccinationDosesCardFactory;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class KeyFigureCardFactory {

  private final Map<Integer, HeaderCardFactory> factoryMap;

  /**
   * Create KeyFigureCardFactory with default FactoryMap. CARD_ID -> HeaderCardFactory.
   */
  public KeyFigureCardFactory() {
    this.factoryMap = Map
        .of(INFECTIONS_CARD_ID, new InfectionsCardFactory(), INCIDENCE_CARD_ID, new IncidenceCardFactory(),
            KEY_SUBMISSION_CARD_ID, new KeySubmissionCardFactory(), REPRODUCTION_NUMBER_CARD,
            new ReproductionNumberCardFactory(), FIRST_VACCINATION_CARD, new FirstVaccinationCardFactory(),
            FULLY_VACCINATED_CARD, new FullyVaccinatedCardFactory(), VACCINATION_DOSES_CARD,
            new VaccinationDosesCardFactory());
  }

  private HeaderCardFactory getFactoryPerCardId(int cardId) {
    return this.factoryMap.getOrDefault(cardId, new EmptyCardFactory());
  }

  public KeyFigureCard createKeyFigureCard(StatisticsJsonStringObject stats, Integer cardId) {
    return this.getFactoryPerCardId(cardId).makeKeyFigureCard(stats);
  }
}
