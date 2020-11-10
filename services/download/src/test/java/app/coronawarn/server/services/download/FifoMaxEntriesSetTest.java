package app.coronawarn.server.services.download;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FifoMaxEntriesSetTest {

  @Test
  void addShouldRespectLimit() {
    FifoMaxEntriesSet<Integer> cut = new FifoMaxEntriesSet<>(1);
    cut.add(5);
    cut.add(3);

    assertThat(cut.contains(5)).isFalse();
  }

  @Test
  void addedShouldBeContained() {
    FifoMaxEntriesSet<Integer> cut = new FifoMaxEntriesSet<>(10);
    cut.add(1);
    cut.add(2);
    cut.add(3);
    cut.add(4);
    cut.add(5);
    cut.add(2);
    cut.add(4);

    assertThat(cut.contains(1)).isTrue();
    assertThat(cut.contains(2)).isTrue();
    assertThat(cut.contains(3)).isTrue();
    assertThat(cut.contains(4)).isTrue();
    assertThat(cut.contains(5)).isTrue();
  }
}
