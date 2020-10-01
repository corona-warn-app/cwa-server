

package app.coronawarn.server.services.submission.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


public class HttpHeaderBuilder {

  private final HttpHeaders headers = new HttpHeaders();

  public static HttpHeaderBuilder builder() {
    return new HttpHeaderBuilder();
  }

  public HttpHeaderBuilder contentTypeProtoBuf() {
    headers.setContentType(MediaType.valueOf("application/x-protobuf"));
    return this;
  }

  public HttpHeaderBuilder cwaAuth() {
    headers.set("cwa-authorization", "TAN okTan");
    return this;
  }

  public HttpHeaderBuilder withCwaFake() {
    headers.set("cwa-fake", "1");
    return this;
  }

  public HttpHeaderBuilder withoutCwaFake() {
    headers.set("cwa-fake", "0");
    return this;
  }

  public HttpHeaders build() {
    return headers;
  }
}
