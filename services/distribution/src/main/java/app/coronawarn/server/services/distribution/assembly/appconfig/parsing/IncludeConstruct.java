package app.coronawarn.server.services.distribution.assembly.appconfig.parsing;

import app.coronawarn.server.services.distribution.assembly.appconfig.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.YamlLoader;
import com.google.protobuf.Message;
import java.nio.file.Path;
import java.util.Arrays;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

public class IncludeConstruct extends AbstractConstruct {

  private final Path path;

  IncludeConstruct(String path) {
    this.path = Path.of(path);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object construct(Node node) {
    ScalarNode scalarNode = (ScalarNode) node;

    Path target = path.getParent().resolve(scalarNode.getValue());

    if (path.equals(target)) {
      throw new IllegalArgumentException("Include references itself.");
    }

    var builder = (Class<? extends Message.Builder>) Arrays.stream(node.getType().getDeclaredClasses())
        .filter(Message.Builder.class::isAssignableFrom)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("No Message.Builder on class: " + node.getType().getSimpleName()));

    try {
      return YamlLoader.loadYamlIntoProtobufBuilder(target.toString(), builder).build();
    } catch (UnableToLoadFileException e) {
      throw new RuntimeException(e);
    }
  }
}
