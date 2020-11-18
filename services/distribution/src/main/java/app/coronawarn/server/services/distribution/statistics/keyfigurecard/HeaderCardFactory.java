package app.coronawarn.server.services.distribution.statistics.keyfigurecard;

import app.coronawarn.server.common.protocols.internal.stats.CardHeader;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.time.ZoneOffset.UTC;

public abstract class HeaderCardFactory {

  public KeyFigureCard makeKeyFigureCard(StatisticsJsonStringObject stats) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate dateTime = LocalDate.parse(stats.getEffectiveDate(), formatter);
    KeyFigureCard.Builder keyFigureBuilder = KeyFigureCard.newBuilder()
        .setHeader(CardHeader.newBuilder()
            .setCardId(this.getCardId())
            .setUpdatedAt(dateTime.atStartOfDay(UTC).toEpochSecond())
            .build()
        );

    return this.buildKeyFigureCard(stats, keyFigureBuilder);
  }

  protected abstract Integer getCardId();

  protected abstract KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats,
      KeyFigureCard.Builder keyFigureBuilder);

}
