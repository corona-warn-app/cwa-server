package app.coronawarn.server.common.shared.util;

import java.io.IOException;
import java.io.InputStream;
import org.everit.json.schema.loader.SchemaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;

public class ResourceSchemaClient implements SchemaClient {

  private static final Logger logger = LoggerFactory.getLogger(ResourceSchemaClient.class);

  private final String path;

  private final ResourceLoader resourceLoader;

  public ResourceSchemaClient(final ResourceLoader resourceLoader, final String path) {
    this.resourceLoader = resourceLoader;
    this.path = path;
  }

  @Override
  public InputStream get(final String url) {
    try {
      final String file = url.substring(url.lastIndexOf('/'));
      return resourceLoader.getResource(path + file).getInputStream();
    } catch (final IOException e) {
      logger.error("can't load '" + url + "'", e);
    }
    return null;
  }
}
