package app.coronawarn.server.services.distribution.assembly.diagnosiskeys.util;

import app.coronawarn.server.common.protocols.external.exposurenotification.File;
import app.coronawarn.server.common.protocols.external.exposurenotification.Header;
import app.coronawarn.server.common.protocols.external.exposurenotification.Key;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Functionality to split collections of {@link Key keys} into similar sized collections of {@link
 * java.io.File files} containing the respective key data.
 */
public class Batch {

  private static final int KILO = 1000;
  private static final int FILE_SIZE_LIMIT_KB = 500;
  private static final int FILE_SIZE_LIMIT_BYTES = FILE_SIZE_LIMIT_KB * KILO;

  /**
   * Aggregates a set of {@link Key Keys} into a set of {@link File Files} of roughly equal size.
   */
  public static Set<File> aggregateKeys(Set<Key> keys, Instant startTimestamp,
      Instant endTimeStamp, String region) {
    // Because protocol buffers optimize each serialization based on the content, we can not exactly
    // calculate the file size that any given serialization will produce ahead of time. So, in order
    // to know into how many batches we will need to split the keys, we simply "attempt" to
    // serialize all keys into a single file and measure its size. If we find that the resulting
    // file goes above the file size limit, we simply partition the keys into N + 1 partitions of
    // approximately equal size, where N = (first file size / file size limit). We add one to N
    // for good measure, to avoid edge cases, rounding errors and varying file sizes after
    // serialization.
    File singleFile = aggregateKeysIntoBatches(keys, 1, startTimestamp, endTimeStamp, region)
        .stream()
        .findFirst()
        .orElseThrow();
    int singleFileSize = singleFile.getSerializedSize();
    if (singleFileSize > FILE_SIZE_LIMIT_BYTES) {
      int numBatches = Maths.ceilDiv(singleFileSize, FILE_SIZE_LIMIT_BYTES) + 1;
      return aggregateKeysIntoBatches(keys, numBatches, startTimestamp, endTimeStamp, region);
    } else {
      return Set.of(singleFile);
    }
  }

  /**
   * Aggregates a set of {@link Key Keys} into a set of equally sized {@link File Files}.
   */
  private static Set<File> aggregateKeysIntoBatches(Set<Key> keys, int numBatches,
      Instant startTimestamp, Instant endTimeStamp, String region) {
    List<Set<Key>> partitions = partitionSet(keys, numBatches);
    return IntStream.range(0, partitions.size())
        .mapToObj(index -> {
          Header header = Header.newBuilder()
              .setStartTimestamp(startTimestamp.toEpochMilli())
              .setEndTimestamp(endTimeStamp.toEpochMilli())
              .setRegion(region)
              .setBatchNum(index + 1)
              .setBatchSize(numBatches)
              .build();
          Set<Key> partition = partitions.get(index);
          return File
              .newBuilder()
              .setHeader(header)
              .addAllKeys(partition)
              .build();
        })
        .collect(Collectors.toSet());
  }

  /**
   * Partitions a set into {@code numPartitions} equally sized sets.
   *
   * @param set           The set to partition
   * @param numPartitions The number of partitions
   * @return A list of sets of equal size
   */
  private static <T> List<Set<T>> partitionSet(Set<T> set, int numPartitions) {
    int partitionSize = Maths.ceilDiv(set.size(), numPartitions);
    List<T> list = new ArrayList<>(set);
    return IntStream.range(0, numPartitions)
        .mapToObj(currentPartition -> new HashSet<>(list.subList(partitionSize * currentPartition,
            Math.min(currentPartition * partitionSize + partitionSize, list.size()))))
        .collect(Collectors.toList());
  }
}
