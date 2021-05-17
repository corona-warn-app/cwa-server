

package app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.decorator;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.common.shared.util.TimeUtils;
import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.TraceTimeIntervalWarningsPackageBundler;
import app.coronawarn.server.services.distribution.assembly.tracewarnings.structure.directory.TraceTimeIntervalWarningsHourDirectory;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.Api;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
class HourIntervalIndexingDecoratorTest {

  @MockBean
  DistributionServiceConfig distributionServiceConfig;

  @MockBean
  CryptoProvider cryptoProvider;

  @MockBean
  TraceTimeIntervalWarningsPackageBundler traceTimeIntervalWarningsPackageBundler;

  HourIntervalIndexingDecorator underTest;

  @BeforeEach
  public void setup() {
    Api api = mock(Api.class);
    when(api.getOriginCountry()).thenReturn("DE");
    when(api.getHourPath()).thenReturn("hour");
    when(distributionServiceConfig.getApi()).thenReturn(api);
    when(distributionServiceConfig.getOutputFileName()).thenReturn("index");
    when(distributionServiceConfig.getSupportedCountries()).thenReturn(new String[]{"DE"});

    underTest = makeDecoratedHourDirectory();
  }


  @AfterEach
  void tearDown() {
    TimeUtils.setNow(null);
  }

  @ParameterizedTest
  @MethodSource("createOldestAndLatest")
  void testEmptyFileIsCreated(List<Optional<Integer>> values) throws Exception {
    //given
    ObjectMapper objectMapper = new ObjectMapper();
    when(traceTimeIntervalWarningsPackageBundler.getOldestHourWithDistributableWarnings(anyString()))
        .thenReturn(values.get(0));
    when(traceTimeIntervalWarningsPackageBundler.getLatestHourWithDistributableWarnings(anyString()))
        .thenReturn(values.get(1));

    //when
    final FileOnDisk indexFile = underTest.getIndexFile("test.json", new ImmutableStack<>().push("DE"));

    //then
    final JSONObject jsonObject = objectMapper.readValue(indexFile.getBytes(), JSONObject.class);
    Assertions.assertThat(jsonObject.size()).isEqualTo(2);
    Assertions.assertThat(jsonObject.get("oldest")).isNull();
    Assertions.assertThat(jsonObject.get("latest")).isNull();
  }

  private static Stream<List<Optional<Integer>>> createOldestAndLatest() {
    return Stream.of(
        List.of(Optional.empty(), Optional.empty()),
        List.of(Optional.ofNullable(null), Optional.ofNullable(null))
    );
  }

  private HourIntervalIndexingDecorator makeDecoratedHourDirectory() {
    return new HourIntervalIndexingDecorator(
        new TraceTimeIntervalWarningsHourDirectory(traceTimeIntervalWarningsPackageBundler, cryptoProvider,
            distributionServiceConfig),
        traceTimeIntervalWarningsPackageBundler,
        distributionServiceConfig);
  }
}
