package app.coronawarn.server.services.eventregistration.testdata;

import app.coronawarn.server.common.protocols.internal.evreg.TraceLocation;
import com.google.protobuf.ByteString;
import java.util.UUID;

public final class TestData {

  public static class TraceLocationBuilder {

    private String address;
    private int checkInLength;
    private String description;
    private String guid;
    private long startTimestamp;
    private long endTimestamp;
    private int version;


    public TraceLocationBuilder withVersion(int version) {
      this.version = version;
      return this;
    }

    public TraceLocationBuilder withAddress(String address) {
      this.address = address;
      return this;
    }

    public TraceLocationBuilder withDefaultCheckInLength(int checkInLength) {
      this.checkInLength = checkInLength;
      return this;
    }

    public TraceLocationBuilder withDescription(String description) {
      this.description = description;
      return this;
    }

    public TraceLocationBuilder withGuid(String guid) {
      this.guid = guid;
      return this;
    }

    public TraceLocationBuilder withStartTimestamp(long startTime) {
      this.startTimestamp = startTime;
      return this;
    }

    public TraceLocationBuilder withEndTimestamp(long endTime) {
      this.endTimestamp = endTime;
      return this;
    }

    public TraceLocation build() {
      return TraceLocation.newBuilder()
          .setAddress(this.address)
          .setDefaultCheckInLengthInMinutes(this.checkInLength)
          .setDescription(this.description)
          .setGuid(ByteString.copyFromUtf8(this.guid))
          .setStartTimestamp(this.startTimestamp)
          .setEndTimestamp(this.endTimestamp)
          .setVersion(this.version)
          .build();
    }

    public TraceLocationBuilder withEmptyGuid() {
      this.guid = "";
      return this;
    }
  }


  public static TraceLocationBuilder traceLocation() {
    return new TraceLocationBuilder();
  }

  public static String buildUuid() {
    return UUID.randomUUID().toString();
  }


}
