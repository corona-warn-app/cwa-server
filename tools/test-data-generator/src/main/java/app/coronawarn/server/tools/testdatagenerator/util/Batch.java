package app.coronawarn.server.tools.testdatagenerator.util;

import app.coronawarn.server.common.protocols.external.exposurenotification.File;
import app.coronawarn.server.common.protocols.external.exposurenotification.Header;
import app.coronawarn.server.common.protocols.external.exposurenotification.Key;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Batch {

  private static final int KILO = 1000;
  private static final int FILE_SIZE_LIMIT_KB = 500;
  private static final int FILE_SIZE_LIMIT_BYTES = FILE_SIZE_LIMIT_KB * KILO;

  /**
   * Aggregates a list of {@link Key Keys} into a list of {@link File Files}.
   *
   * @return A list of lists of equal size
   */
  public static List<File> aggregateKeys(List<Key> keys, Instant startTimestamp,
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
      return List.of(singleFile);
    }
  }

  /**
   * Aggregates a list of {@link Key Keys} into a list of equally sized {@link File Files} with
   * length {@code partitions}.
   */
  private static List<File> aggregateKeysIntoBatches(List<Key> keys, int numBatches,
      Instant startTimestamp, Instant endTimeStamp, String region) {
    List<List<Key>> partitions = partitionList(keys, numBatches);
    return IntStream.range(0, partitions.size())
        .mapToObj(index -> {
          Header header = Header.newBuilder()
              .setStartTimestamp(startTimestamp.toEpochMilli())
              .setEndTimestamp(endTimeStamp.toEpochMilli())
              .setRegion(region)
              .setBatchNum(index + 1)
              .setBatchSize(numBatches)
              .build();
          List<Key> partition = partitions.get(index);
          return File
              .newBuilder()
              .setHeader(header)
              .addAllKeys(partition)
              .build();
        })
        .collect(Collectors.toList());
  }

  /**
   * Partitions a list into {@code numPartitions} equally sized lists.
   *
   * @param list          The list to partition
   * @param numPartitions The number of partitions
   * @return A list of lists of equal size
   */
  public static <T> List<List<T>> partitionList(List<T> list, int numPartitions) {
    int partitionSize = Maths.ceilDiv(list.size(), numPartitions);
    return IntStream.range(0, numPartitions)
        .mapToObj(currentPartition -> list.subList(partitionSize * currentPartition,
            Math.min(currentPartition * partitionSize + partitionSize, list.size())))
        .collect(Collectors.toList());
  }
}
