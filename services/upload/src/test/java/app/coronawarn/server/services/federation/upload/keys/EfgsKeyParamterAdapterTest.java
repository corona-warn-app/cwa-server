package app.coronawarn.server.services.federation.upload.keys;

import static org.assertj.core.api.Assertions.*;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.services.federation.upload.utils.UploadKeysMockData;

class EfgsKeyParamterAdapterTest {

  @Test
  void shouldRemoveOriginCountryFromVisitedCountriesIfPresent() {
    List<FederationUploadKey> testKeys = List.of(
            UploadKeysMockData.generateRandomUploadKey("DE", Set.of("DE","FR")),
            UploadKeysMockData.generateRandomUploadKey("FR", Set.of("DE","FR")));

    EfgsKeyParameterAdapter keyAdapter = new EfgsKeyParameterAdapter();
    List<FederationUploadKey> uploadableKeys = keyAdapter.adaptToEfgsRequirements(testKeys);
    uploadableKeys.stream().forEach( (uploadKey) -> {
      assertThat(uploadKey.getVisitedCountries()).doesNotContain(uploadKey.getOriginCountry());
      // make sure other countries were not removed
      assertThat(uploadKey.getVisitedCountries()).hasSize(1);
    });
  }

  @Test
  void shouldNotChangeVisitedCountriesIfOriginIsNotContainedInTheSet() {
    List<FederationUploadKey> testKeys = List.of(
            UploadKeysMockData.generateRandomUploadKey("DE", Set.of("DK","FR")),
            UploadKeysMockData.generateRandomUploadKey("FR", Set.of("DE","DK")));

    EfgsKeyParameterAdapter keyAdapter = new EfgsKeyParameterAdapter();
    List<FederationUploadKey> uploadableKeys = keyAdapter.adaptToEfgsRequirements(testKeys);
    uploadableKeys.stream().forEach( (uploadKey) -> {
      assertThat(uploadKey.getVisitedCountries()).hasSize(2);
    });
  }
}
