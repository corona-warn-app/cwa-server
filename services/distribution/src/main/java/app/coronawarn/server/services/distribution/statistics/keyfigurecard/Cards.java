package app.coronawarn.server.services.distribution.statistics.keyfigurecard;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.BoosterVaccinatedCardFactory;
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
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.LinkCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.ReproductionNumberCardFactory;
import app.coronawarn.server.services.distribution.statistics.keyfigurecard.factory.VaccinationDosesCardFactory;

public enum Cards {

  /**
   * Empty card.
   */
  EMPTY_CARD("EMPTY CARD"),
  /**
   * Infections Card.
   */
  INFECTIONS_CARD("Infections Card"),
  /**
   * Incidence Card.
   */
  INCIDENCE_CARD("Incidence Card"),
  /**
   * Key Submission Card.
   */
  KEY_SUBMISSION_CARD("Key Submission Card"),
  /**
   * Reproduction Number Card.
   */
  REPRODUCTION_NUMBER_CARD("Reproduction Number Card"),
  /**
   * First Vaccination Card.
   */
  FIRST_VACCINATION_CARD("First Vaccination Card"),
  /**
   * Fully Vaccincated Card.
   */
  FULLY_VACCINATED_CARD("Fully Vaccincated Card"),
  /**
   * Vaccination Doses Card.
   */
  VACCINATION_DOSES_CARD("Vaccination Doses Card"),
  /**
   * Hospitalization Incidence Card.
   */
  HOSPITALIZATION_INCIDENCE_CARD("Hospitalization Incidence Card"),
  /**
   * Intensive Care Card.
   */
  INTENSIVE_CARE_CARD("Intensive Care Card"),
  /**
   * Joined Incidence Card.
   */
  JOINED_INCIDENCE_CARD("Joined incidence Card"),
  /**
   * Third Dose Card.
   */
  BOOSTER_VACCINATED_CARD("Booster Vaccinated Card"),
  /**
   * Outdated pandemic radar URL card.
   */
  PANDEMIC_RADAR_CARD("Link Card"),
  /**
   * New pandemic radar (BMG) URL card.
   */
  PANDEMIC_RADAR_BMG_CARD("Pandemic Radar Card (BMG)"),
  ;

  /**
   * Get card factory by ID.
   *
   * @param card   {@link #ordinal()}
   * @param config The distribution configuration used to get the infection threshold parameter.
   * @return {@link #getFactory()}
   */
  public static HeaderCardFactory getFactoryFor(final Cards card, final DistributionServiceConfig config) {
    switch (card) {
      case INFECTIONS_CARD:
        return new InfectionsCardFactory(config);
      case INCIDENCE_CARD:
        return new IncidenceCardFactory(config);
      case KEY_SUBMISSION_CARD:
        return new KeySubmissionCardFactory(config);
      case REPRODUCTION_NUMBER_CARD:
        return new ReproductionNumberCardFactory(config);
      case FIRST_VACCINATION_CARD:
        return new FirstVaccinationCardFactory(config);
      case FULLY_VACCINATED_CARD:
        return new FullyVaccinatedCardFactory(config);
      case VACCINATION_DOSES_CARD:
        return new VaccinationDosesCardFactory(config);
      case HOSPITALIZATION_INCIDENCE_CARD:
        return new HospitalizationIncidenceCardFactory(config);
      case INTENSIVE_CARE_CARD:
        return new IntensiveCareCardFactory(config);
      case JOINED_INCIDENCE_CARD:
        return new JoinedIncidenceCardFactory(config);
      case BOOSTER_VACCINATED_CARD:
        return new BoosterVaccinatedCardFactory(config);
      case PANDEMIC_RADAR_CARD:
        return new LinkCardFactory(config, PANDEMIC_RADAR_CARD);
      case PANDEMIC_RADAR_BMG_CARD:
        return new LinkCardFactory(config, PANDEMIC_RADAR_BMG_CARD);
      default:
        return new EmptyCardFactory(config);
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

  final String name;

  Cards(final String name) {
    this.name = name;
  }

  String getName() {
    return name;
  }
}
