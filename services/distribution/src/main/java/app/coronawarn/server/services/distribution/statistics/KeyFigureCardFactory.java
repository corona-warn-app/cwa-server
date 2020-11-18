package app.coronawarn.server.services.distribution.statistics;

import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.protocols.internal.stats.CardHeader;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Rank;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.Trend;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigure.TrendSemantic;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard;
import app.coronawarn.server.common.protocols.internal.stats.KeyFigureCard.Builder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class KeyFigureCardFactory {

  private final ValueProcessor valueProcessor;

  public KeyFigureCardFactory(ValueProcessor valueProcessor) {
    this.valueProcessor = valueProcessor;
  }

  public KeyFigureCard createKeyFigureCard(StatisticsJsonStringObject stats, Integer cardId) {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate dateTime = LocalDate.parse(stats.getEffectiveDate(), formatter);

    KeyFigureCard.Builder keyFigureBuilder = KeyFigureCard.newBuilder()
        .setHeader(CardHeader.newBuilder()
            .setCardId(cardId)
            .setUpdatedAt(dateTime.atStartOfDay(UTC).toEpochSecond())
            .build()
        );

    switch (cardId) {
      case 1:
        return createInfectionCard(stats, keyFigureBuilder);
      case 2:
        return createIncidenceCard(keyFigureBuilder);
      case 3:
        return createSubmissionCard(keyFigureBuilder);
      case 4:
        return createEmptyCard(keyFigureBuilder);
      default:
        break;
    }
    return null;
  }

  private KeyFigureCard createEmptyCard(Builder keyFigureBuilder) {
    return keyFigureBuilder.build();
  }

  private KeyFigureCard createInfectionCard(StatisticsJsonStringObject stats,
      KeyFigureCard.Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(List.of(
        KeyFigure.newBuilder()
            .setValue(stats.getInfectionsReportedDaily())
            .setRank(Rank.PRIMARY)
            .setDecimals(0)
            .setTrend(Trend.UNSPECIFIED_TREND)
            .setTrendSemantic(TrendSemantic.UNSPECIFIED_TREND_SEMANTIC)
            .build(),
        KeyFigure.newBuilder()
            .setValue(stats.getInfectionsReported7daysAvg())
            .setRank(Rank.SECONDARY)
            .setDecimals(0)
            .setTrend(Trend.INCREASING)
            .setTrendSemantic(TrendSemantic.NEGATIVE)
            .build(),
        KeyFigure.newBuilder()
            .setValue(stats.getInfectionsReportedCumulated())
            .setRank(Rank.TERTIARY)
            .setDecimals(0)
            .setTrend(Trend.UNSPECIFIED_TREND)
            .setTrendSemantic(TrendSemantic.UNSPECIFIED_TREND_SEMANTIC)
            .build())).build();
  }

  private KeyFigureCard createIncidenceCard(KeyFigureCard.Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(List.of(KeyFigure.newBuilder().build())).build();
  }

  private KeyFigureCard createSubmissionCard(KeyFigureCard.Builder keyFigureBuilder) {
    return keyFigureBuilder.addAllKeyFigures(List.of(KeyFigure.newBuilder().build(),
        KeyFigure.newBuilder().build(), KeyFigure.newBuilder().build())).build();
  }

}
