/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.distribution.assembly.appconfig.parsing;

import app.coronawarn.server.services.distribution.assembly.appconfig.UnableToLoadFileException;
import app.coronawarn.server.services.distribution.assembly.appconfig.YamlLoader;
import com.google.protobuf.Message;
import java.nio.file.Path;
import java.util.Arrays;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

/**
 * This extension to SnakeYaml allows to merge yaml files, e.g.:
 * <pre>
 *   example: !include other.yaml
 * </pre>
 * This construct will follow the relative include based on the parent Yaml file, and will
 * transform the result to the matching Protobuf representation. No other classes are
 * allowed.
 */
public class IncludeConstruct extends AbstractConstruct {

  /** the path of the parent Yaml. */
  private final Path path;

  /**
   * Creates a new include construct, to be used in a {@link Constructor}, e.g.:
   *
   * <pre>
   *  this.yamlConstructors.put(new Tag("!include"), new IncludeConstruct(path));
   * </pre>
   * @param path the path of the parent Yaml.
   */
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
        .orElseThrow(() -> new NoMessageBuilderOnClassException(node));

    try {
      return YamlLoader.loadYamlIntoProtobufBuilder(target.toString(), builder).build();
    } catch (UnableToLoadFileException e) {
      throw new IncludeResolveFailedException(scalarNode, e);
    }
  }
}
