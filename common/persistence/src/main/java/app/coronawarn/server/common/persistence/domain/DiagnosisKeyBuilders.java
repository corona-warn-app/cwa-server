package app.coronawarn.server.common.persistence.domain;

import app.coronawarn.server.common.protocols.external.exposurenotification.Key;

/**
 * This interface bundles interfaces that are used for the implementation of {@link
 * DiagnosisKeyBuilder}.
 */
interface DiagnosisKeyBuilders {

  interface Builder {

    /**
     * Adds the specified key data to this builder.
     *
     * @param keyData generated diagnosis key.
     * @return this Builder instance.
     */
    RollingStartNumberBuilder withKeyData(byte[] keyData);

    /**
     * Adds the data contained in the specified protocol buffers key object to this builder.
     *
     * @param protoBufObject ProtocolBuffer object associated with the temporary exposure key.
     * @return this Builder instance.
     */
    FinalBuilder fromProtoBuf(Key protoBufObject);
  }

  interface RollingStartNumberBuilder {

    /**
     * Adds the specified rolling start number to this builder.
     *
     * @param rollingStartNumber number describing when a key starts. It is equal to
     *                           startTimeOfKeySinceEpochInSecs / (60 * 10).
     * @return this Builder instance.
     */
    RollingPeriodBuilder withRollingStartNumber(long rollingStartNumber);
  }

  interface RollingPeriodBuilder {

    /**
     * Adds the specified rolling period to this builder.
     *
     * @param rollingPeriod Number describing how long a key is valid. It is expressed in increments
     *                      of 10 minutes (e.g. 144 for 24 hours).
     * @return this Builder instance.
     */
    TransmissionRiskLevelBuilder withRollingPeriod(long rollingPeriod);
  }

  interface TransmissionRiskLevelBuilder {

    /**
     * Adds the specified transmission risk level to this builder.
     *
     * @param transmissionRiskLevel risk of transmission associated with the person this key came
     *                              from.
     * @return this Builder instance.
     */
    FinalBuilder withTransmissionRiskLevel(int transmissionRiskLevel);
  }

  interface FinalBuilder {

    /**
     * Adds the specified submission timestamp that is expected to represent hours since epoch.
     *
     * @param submissionTimestamp timestamp in hours since epoch.
     * @return this Builder instance.
     */
    FinalBuilder withSubmissionTimestamp(long submissionTimestamp);

    /**
     * Builds a {@link DiagnosisKey} instance. If no submission timestamp has been specified it will
     * be set to "now" as hours since epoch.
     */
    DiagnosisKey build();
  }
}
