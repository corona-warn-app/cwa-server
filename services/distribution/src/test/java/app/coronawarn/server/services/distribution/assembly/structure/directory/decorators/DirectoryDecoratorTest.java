/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.distribution.assembly.structure.directory.decorators;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.DirectoryDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileImpl;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import org.junit.jupiter.api.Test;

public class DirectoryDecoratorTest {

  @Test
  public void checkProxiesAllMethods() {
    Directory decoratee = mock(Directory.class);
    Directory decorator = new TestDirectoryDecorator(decoratee);

    ImmutableStack<Object> stack = new ImmutableStack<>();
    decorator.prepare(stack);
    verify(decoratee).prepare(stack);

    File file = new FileImpl("foo", new byte[0]);
    decorator.addFile(file);
    verify(decoratee).addFile(file);

    decorator.getFiles();
    verify(decoratee).getFiles();

    Directory directory = new DirectoryImpl("foo");
    decorator.addDirectory(directory);
    verify(decoratee).addDirectory(directory);

    decorator.getDirectories();
    verify(decoratee).getDirectories();

    decorator.write();
    verify(decoratee).write();

    decorator.getName();
    verify(decoratee).getName();

    decorator.getParent();
    verify(decoratee).getParent();

    decorator.setParent(directory);
    verify(decoratee).setParent(directory);

    decorator.getFileOnDisk();
    verify(decoratee).getFileOnDisk();
  }

  private static class TestDirectoryDecorator extends DirectoryDecorator {

    protected TestDirectoryDecorator(Directory directory) {
      super(directory);
    }
  }
}
