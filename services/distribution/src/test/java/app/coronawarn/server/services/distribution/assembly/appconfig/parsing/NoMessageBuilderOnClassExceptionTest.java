

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
