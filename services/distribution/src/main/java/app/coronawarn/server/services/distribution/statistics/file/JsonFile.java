package app.coronawarn.server.services.distribution.statistics.file;

import java.io.InputStream;

public class JsonFile {
  private InputStream content;
  private String etag;

  public JsonFile(final InputStream content, final String etag) {
    this.content = content;
    this.etag = etag;
  }

  public InputStream getContent() {
    return content;
  }

  public String getETag() {
    return etag;
  }

  public void setContent(final InputStream content) {
    this.content = content;
  }

  public void setETag(final String etag) {
    this.etag = etag;
  }
}
