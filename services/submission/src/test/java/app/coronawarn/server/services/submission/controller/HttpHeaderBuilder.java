package app.coronawarn.server.services.submission.controller;

import static org.springframework.http.MediaType.valueOf;

import org.springframework.http.HttpHeaders;

public class HttpHeaderBuilder {

  public static HttpHeaderBuilder builder() {
    return new HttpHeaderBuilder();
  }

  private final HttpHeaders headers = new HttpHeaders();

  public HttpHeaders build() {
    return headers;
  }

  public HttpHeaderBuilder contentTypeProtoBuf() {
    headers.setContentType(valueOf("application/x-protobuf"));
    return this;
  }

  public HttpHeaderBuilder cwaAuth() {
    headers.set("cwa-authorization", "TAN okTan");
    return this;
  }

  public HttpHeaderBuilder cwaOtp() {
    headers.set("cwa-otp", "OTP ok");
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
}
