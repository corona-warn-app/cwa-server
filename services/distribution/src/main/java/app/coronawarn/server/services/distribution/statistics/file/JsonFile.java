package app.coronawarn.server.services.distribution.statistics.file;

public class JsonFile {
  private String content;
  private String eTag;

  public JsonFile(String content, String eTag) {
    this.content = content;
    this.eTag = eTag;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getETag() {
    return eTag;
  }

  public void setETag(String eTag) {
    this.eTag = eTag;
  }
}
