package app.coronawarn.server.services.distribution.statistics;

import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.protocols.internal.stats.CardHeader;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.EmptyCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.HeaderCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.IncidenceCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.InfectionsCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.KeySubmissionCardFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class KeyFigureCardFactory {

  private final ValueProcessor valueProcessor;
  public KeyFigureCardFactory(ValueProcessor valueProcessor) {
    this.valueProcessor = valueProcessor;
  }

  private HeaderCardFactory getFactoryPerCardId(int cardId) {
    switch (cardId) {
      case 1:
        return new InfectionsCardFactory();
      case 2:
        return new IncidenceCardFactory();
      case 3:
        return new KeySubmissionCardFactory();
      default:
        return new EmptyCardFactory();
    }
  }

  public KeyFigureCard createKeyFigureCard(StatisticsJsonStringObject stats, Integer cardId) {
    return this.getFactoryPerCardId(cardId).makeKeyFigureCard(stats);
  }
}
