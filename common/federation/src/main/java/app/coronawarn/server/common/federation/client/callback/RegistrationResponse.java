package app.coronawarn.server.common.federation.client.callback;

/**
 * Callback Registration response is returned from the EFGS and contains information about endpoints registered in its
 * callback API.
 */
public class RegistrationResponse {

  private String id;
  private String url;

  public RegistrationResponse() {
    // empty constructor
  }

  public RegistrationResponse(String id, String url) {
    this.id = id;
    this.url = url;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
