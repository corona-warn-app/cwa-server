package app.coronawarn.server.services.distribution.statistics.file;

public class JsonFile {
  private String content;
  private String etag;

  public JsonFile(String content, String etag) {
    this.content = content;
    this.etag = etag;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getETag() {
    return etag;
  }

  public void setETag(String etag) {
    this.etag = etag;
  }
}
