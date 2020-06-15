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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.nodes.Node;

class NoMessageBuilderOnClassExceptionTest {

  @Test
  void testCorrectMessage() {
    Node node = mock(Node.class);
    Class expType = String.class;
    when(node.getType()).thenReturn(expType);
    NoMessageBuilderOnClassException actException = new NoMessageBuilderOnClassException(node);
    assertThat(actException.getMessage()).isEqualTo("No Message.Builder on class: " + expType.getSimpleName());
  }
}
