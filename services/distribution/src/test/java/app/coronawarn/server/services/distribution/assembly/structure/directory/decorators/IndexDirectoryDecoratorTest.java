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

import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.IndexDirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.decorator.IndexDirectoryDecorator;
import org.junit.jupiter.api.Test;

public class IndexDirectoryDecoratorTest {

  @Test
  public void checkProxiesAllMethods() {
    IndexDirectory<?, WritableOnDisk> decoratee = mock(IndexDirectoryOnDisk.class);
    IndexDirectory<?, WritableOnDisk> decorator = new TestIndexDirectoryDecorator<>(decoratee);

    // TODO
  }

  private static class TestIndexDirectoryDecorator<T> extends IndexDirectoryDecorator<T, WritableOnDisk> {

    protected TestIndexDirectoryDecorator(IndexDirectory<T, WritableOnDisk> directory) {
      super(directory);
    }
  }
}