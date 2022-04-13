package app.coronawarn.server.services.distribution.dcc;

import app.coronawarn.server.common.persistence.domain.RevocationEntry;
import app.coronawarn.server.common.persistence.service.DccRevocationListService;
import app.coronawarn.server.common.protocols.internal.dgc.RevocationChunk;
import app.coronawarn.server.common.protocols.internal.dgc.RevocationKidList;
import app.coronawarn.server.common.protocols.internal.dgc.RevocationKidListItem;
import app.coronawarn.server.common.protocols.internal.dgc.RevocationKidTypeIndex;
import app.coronawarn.server.common.protocols.internal.dgc.RevocationKidTypeIndexItem;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DccRevocationListToProtobufMapping {

  private final DccRevocationListService dccRevocationListService;


  public DccRevocationListToProtobufMapping(DccRevocationListService dccRevocationListService) {
    this.dccRevocationListService = dccRevocationListService;
  }

  public RevocationKidList constructProtobufMappingKidList(Map<Integer, List<RevocationEntry>> revocationEntryList) {

    return RevocationKidList.newBuilder().addAllItems(buildItemList(revocationEntryList)).build();
  }

  private List<RevocationKidListItem> buildItemList(Map<Integer, List<RevocationEntry>> revocationEntriesByKidAndHash) {
    List<RevocationKidListItem> revocationKidListItems = new ArrayList<>();
    revocationEntriesByKidAndHash.keySet().forEach(kid -> {
      Set<ByteString> types = new HashSet<>();
      revocationEntriesByKidAndHash.get(kid).forEach(revocationEntry -> {
        types.add(ByteString.copyFrom(revocationEntry.getType()));
      });
      revocationKidListItems.add(
          RevocationKidListItem.newBuilder()
              .setKid(ByteString.copyFrom(revocationEntriesByKidAndHash.get(kid).get(0).getKid()))
              .addAllHashTypes(types)
              .build());
    });
    return revocationKidListItems;
  }

  /**
   * Construct RevocationKidTypeIndex.
   */
  public RevocationKidTypeIndex constructProtobufMappingKidType(List<RevocationEntry> revocationEntries) {
    Map<Integer, List<RevocationEntry>> revocationEntriesGrouped = revocationEntries.stream()
        .collect(Collectors.groupingBy(RevocationEntry::getXHash));
    List<RevocationKidTypeIndexItem> revocationKidTypeIndexItems = new ArrayList<>();
    revocationEntriesGrouped.keySet().forEach(revocationBasedOnxHash -> {
      List<ByteString> hashesForY = revocationEntriesGrouped.get(revocationBasedOnxHash).stream()
          .map(revocationEntry -> ByteString.copyFrom(revocationEntry.getYhash())).collect(Collectors.toList());
      revocationKidTypeIndexItems.add(RevocationKidTypeIndexItem.newBuilder()
          .setX(ByteString.copyFrom(revocationEntriesGrouped.get(revocationBasedOnxHash).get(0).getXhash()))
          .addAllY(hashesForY).build());
    });
    return RevocationKidTypeIndex.newBuilder().addAllItems(revocationKidTypeIndexItems).build();
  }

  public RevocationChunk constructProtobufMappingChunkList(List<RevocationEntry> yhashRevocationEntryList) {
    return RevocationChunk.newBuilder().addAllHashes(yhashRevocationEntryList.stream().map(
        revocationEntry -> ByteString.copyFrom(revocationEntry.getHash())).collect(Collectors.toList())).build();
  }
}
