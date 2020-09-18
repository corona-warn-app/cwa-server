

package app.coronawarn.server.services.federation.upload.client;

import app.coronawarn.server.services.federation.upload.payload.UploadPayload;

public interface FederationUploadClient {

  void postBatchUpload(UploadPayload uploadPayload);

}
