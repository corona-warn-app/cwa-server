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

import org.yaml.snakeyaml.nodes.Node;

/**
 * Indicates, that the target property for a Yaml {@link IncludeConstruct} is not a valid Protobuf Message, because they
 * should contain a Message Builder. This Message.Builder is required for the processing.
 */
public class NoMessageBuilderOnClassException extends RuntimeException {

  /**
   * Creates a new exception instance based on the given {@link Node}.
   *
   * @param node the node, which points to a non-Protobuf message type.
   */
  public NoMessageBuilderOnClassException(Node node) {
    super(String.format("No Message.Builder on class: %s", node.getType().getSimpleName()));
  }
}
