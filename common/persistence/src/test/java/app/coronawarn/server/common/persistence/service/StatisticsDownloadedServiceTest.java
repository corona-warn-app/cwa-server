package app.coronawarn.server.common.persistence.service;

import app.coronawarn.server.common.persistence.domain.StatisticsDownloaded;
import app.coronawarn.server.common.persistence.repository.StatisticsDownloadRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DataJdbcTest
public class StatisticsDownloadedServiceTest {

  @Autowired
  private StatisticsDownloadService statisticsDownloadService;

  @MockBean
  private StatisticsDownloadRepository statisticsDownloadRepository;

  @Test
  void shouldReturnOptionalEmptyWhenNoRecordsFound() {
    when(statisticsDownloadRepository.getWithLatestETag()).thenReturn(null);
    assertThat(statisticsDownloadService.getMostRecentDownload()).isEmpty();
  }

  @Test
  void shouldReturnDownloadRecordIfStored() {
    when(statisticsDownloadRepository.getWithLatestETag()).thenReturn(new StatisticsDownloaded(1, 1, "a"));
    assertThat(statisticsDownloadService.getMostRecentDownload()).isNotEmpty();
  }

  @Test
  void shouldReturnTrueWhenStoredSuccessfully() {
    assertThat(statisticsDownloadService.store(123, "etag")).isTrue();
  }

  @Test
  void shouldReturnFalseWhenFailedToStore() {
    doThrow(new RuntimeException())
        .when(statisticsDownloadRepository)
        .insertWithAutoIncrement(anyLong(), anyString());
    assertThat(statisticsDownloadService.store(123, "etag")).isFalse();
  }

  @Test
  void shouldApplyRetentionPolicyEvenWhenNoRecordsAreFound() {
    when(statisticsDownloadRepository.countDownloadEntriesOlderThan(anyLong()))
        .thenReturn(0);
    statisticsDownloadService.applyRetentionPolicy(14);
    verify(statisticsDownloadRepository, times(1))
        .deleteDownloadEntriesOlderThan(anyLong());
  }

}
