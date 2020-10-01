

package app.coronawarn.server.services.federation.upload.client;

import app.coronawarn.server.common.federation.client.upload.BatchUploadResponse;
import app.coronawarn.server.services.federation.upload.payload.UploadPayload;

public interface FederationUploadClient {

  BatchUploadResponse postBatchUpload(UploadPayload uploadPayload);

}
