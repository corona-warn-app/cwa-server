package app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory;

import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.protocols.internal.stats.CardHeader;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.services.distribution.statistics.StatisticsJsonStringObject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class HeaderCardFactory {

  /**
   * Create KeyFigureCard object. Calls the children method `buildKeyFigureCard` for card specific properties. This
   * method adds the generic CardHeader that all KeyFigureCards must have.
   *
   * @param stats JSON Object statistics
   * @return KeyFigureCard .
   */
  public KeyFigureCard makeKeyFigureCard(StatisticsJsonStringObject stats) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate dateTime = LocalDate.parse(stats.getEffectiveDate(), formatter);
    KeyFigureCard.Builder keyFigureBuilder = makeBuilderWithDefaultHeader(dateTime);
    throwIfNullFieldsFound(stats);
    return this.buildKeyFigureCard(stats, keyFigureBuilder);
  }

  private KeyFigureCard.Builder makeBuilderWithDefaultHeader(LocalDate dateTime) {
    return KeyFigureCard.newBuilder()
        .setHeader(CardHeader.newBuilder()
            .setCardId(this.getCardId())
            .setUpdatedAt(dateTime.atStartOfDay(UTC).toEpochSecond())
            .build()
        );
  }

  private void throwIfNullFieldsFound(StatisticsJsonStringObject stats) {
    var nullFieldsOrZeroOrLessThanZero = getRequiredFieldValues(stats).stream()
        .filter(Optional::isEmpty)
        .collect(Collectors.toList());
    if (!nullFieldsOrZeroOrLessThanZero.isEmpty()) {
      throw new MissingPropertyException(this.getCardId());
    }
  }

  protected abstract int getCardId();

  protected abstract KeyFigureCard buildKeyFigureCard(StatisticsJsonStringObject stats,
      KeyFigureCard.Builder keyFigureBuilder);

  /**
   * Return the list of required fields to create this card. Implemented by factories. If any of the fields returned by
   * this method is Null or <= 0, a MissingPropertyException will be thrown and the card
   * will be skipped for given day.
   *
   * @param stats JSON string object.
   * @return List of objects to be checked if are null.
   */
  protected abstract List<Optional<Object>> getRequiredFieldValues(StatisticsJsonStringObject stats);
}
