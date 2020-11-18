package app.coronawarn.server.services.distribution.statistics.keyfigurecard;

import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.FOURTH_CARD_ID;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.INCIDENCE_CARD_ID;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.INFECTIONS_CARD_ID;
import static app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeyFigureCardSequenceConstants.KEY_SUBMISSION_CARD_ID;

import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import app.coronawarn.server.services.distribution.statistics.ValueProcessor;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.EmptyCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.HeaderCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.IncidenceCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.InfectionsCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.KeySubmissionCardFactory;

public class KeyFigureCardFactory {

  private final ValueProcessor valueProcessor;

  public KeyFigureCardFactory(ValueProcessor valueProcessor) {
    this.valueProcessor = valueProcessor;
  }

  private HeaderCardFactory getFactoryPerCardId(int cardId) {
    switch (cardId) {
      case INFECTIONS_CARD_ID:
        return new InfectionsCardFactory();
      case INCIDENCE_CARD_ID:
        return new IncidenceCardFactory();
      case KEY_SUBMISSION_CARD_ID:
        return new KeySubmissionCardFactory();
      case FOURTH_CARD_ID:
      default:
        return new EmptyCardFactory();
    }
  }

  public KeyFigureCard createKeyFigureCard(StatisticsJsonStringObject stats, Integer cardId) {
    return this.getFactoryPerCardId(cardId).makeKeyFigureCard(stats);
  }
}
