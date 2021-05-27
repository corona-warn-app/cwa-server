package app.coronawarn.server.services.distribution.statistics.keyfigurecard;

public class KeyFigureCardSequenceConstants {

  private KeyFigureCardSequenceConstants() {
  }

  /**
   * Convert Card ID into respective card name.
   * @param cardId the card constant id.
   * @return String name.
   */
  public static String toCardName(int cardId) {
    switch (cardId) {
      case INFECTIONS_CARD_ID:
        return "Infections Card";
      case INCIDENCE_CARD_ID:
        return "Incidence Card";
      case KEY_SUBMISSION_CARD_ID:
        return "Key Submission Card";
      case REPRODUCTION_NUMBER_CARD:
        return "Reproduction Number Card";
      default:
        return "EMPTY CARD";
    }
  }

  public static final int INFECTIONS_CARD_ID = 1;

  public static final int INCIDENCE_CARD_ID = 2;

  public static final int KEY_SUBMISSION_CARD_ID = 3;

  public static final int REPRODUCTION_NUMBER_CARD = 4;

  public static final int FIRST_VACCINATION_CARD = 5;

  public static final int FULLY_VACCINATED_CARD = 6;

  public static final int VACCINATION_DOSES_CARD = 7;

  public static final int EMPTY_CARD = -1;

}
