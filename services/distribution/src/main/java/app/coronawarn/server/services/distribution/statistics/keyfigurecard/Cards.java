package app.coronawarn.server.services.distribution.statistics.keyfigurecard;

import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.EmptyCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.FirstVaccinationCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.FullyVaccinatedCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.HeaderCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.HospitalizationIncidenceCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.IncidenceCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.InfectionsCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.IntensiveCareCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.JoinedIncidenceCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.KeySubmissionCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.ReproductionNumberCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.VaccinationDosesCardFactory;

public enum Cards {

  /**
   * Empty card.
   */
  EMPTY_CARD(new EmptyCardFactory(), "EMPTY CARD"),
  /**
   * Infections Card.
   */
  INFECTIONS_CARD(new InfectionsCardFactory(), "Infections Card"),
  /**
   * Incidence Card.
   */
  INCIDENCE_CARD(new IncidenceCardFactory(), "Incidence Card"),
  /**
   * Key Submission Card.
   */
  KEY_SUBMISSION_CARD(new KeySubmissionCardFactory(), "Key Submission Card"),
  /**
   * Reproduction Number Card.
   */
  REPRODUCTION_NUMBER_CARD(new ReproductionNumberCardFactory(), "Reproduction Number Card"),
  /**
   * First Vaccination Card.
   */
  FIRST_VACCINATION_CARD(new FirstVaccinationCardFactory(), "First Vaccination Card"),
  /**
   * Fully Vaccincated Card.
   */
  FULLY_VACCINATED_CARD(new FullyVaccinatedCardFactory(), "Fully Vaccincated Card"),
  /**
   * Vaccination Doses Card.
   */
  VACCINATION_DOSES_CARD(new VaccinationDosesCardFactory(), "Vaccination Doses Card"),
  /**
   * Hospitalization Incidence Card.
   */
  HOSPITALIZATION_INCIDENCE_CARD(new HospitalizationIncidenceCardFactory(), "Hospitalization Incidence Card"),
  /**
   * Intensive Care Card.
   */
  INTENSIVE_CARE_CARD(new IntensiveCareCardFactory(), "Intensive Care Card"),
  /**
   * Joined Incidence Card.
   */
  JOINED_INCIDENCE_CARD(new JoinedIncidenceCardFactory(), "Joined incidence Card");

  /**
   * Get card factory by ID.
   *
   * @param id {@link #ordinal()}
   * @return {@link #getFactory()}
   */
  public static HeaderCardFactory getFactoryFor(final int id) {
    try {
      return values()[id].getFactory();
    } catch (final ArrayIndexOutOfBoundsException e) {
      return EMPTY_CARD.getFactory();
    }
  }

  /**
   * Get card name by ID.
   *
   * @param id {@link #ordinal()}
   * @return {@link #getName()}
   */
  public static String getNameFor(final int id) {
    try {
      return values()[id].getName();
    } catch (final ArrayIndexOutOfBoundsException e) {
      return "Name not found for Card ID: " + id;
    }
  }

  final HeaderCardFactory factory;

  final String name;

  Cards(final HeaderCardFactory factory, final String name) {
    this.factory = factory;
    this.name = name;
  }

  HeaderCardFactory getFactory() {
    return factory;
  }

  String getName() {
    return name;
  }
}
