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

package app.coronawarn.server.services.distribution.assembly.appconfig;

import app.coronawarn.server.services.distribution.assembly.appconfig.parsing.YamlConstructorForProtoBuf;
import com.google.protobuf.Message;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.BeanAccess;

public class YamlLoader {

  private YamlLoader() {
  }

  /**
   * Returns a protobuf {@link Message.Builder message builder} of the specified type, whose fields have been set to the
   * corresponding values from the yaml file at the specified path.
   *
   * @param path        The absolute path of the yaml file within the class path.
   * @param builderType The specific {@link com.google.protobuf.Message.Builder} implementation that will be returned.
   * @return A prepared protobuf {@link Message.Builder message builder} of the specified type.
   * @throws UnableToLoadFileException if either the file access or subsequent yaml parsing fails.
   */
  public static <T extends Message.Builder> T loadYamlIntoProtobufBuilder(String path, Class<T> builderType)
      throws UnableToLoadFileException {
    Yaml yaml = new Yaml(new YamlConstructorForProtoBuf(path));
    // no setters for generated message classes available
    yaml.setBeanAccess(BeanAccess.FIELD);

    Resource configurationResource = new ClassPathResource(path);
    try (InputStream inputStream = configurationResource.getInputStream()) {
      T loaded = yaml.loadAs(inputStream, builderType);
      if (loaded == null) {
        throw new UnableToLoadFileException(path);
      }

      return loaded;
    } catch (YAMLException e) {
      throw new UnableToLoadFileException("Parsing failed", e);
    } catch (IOException e) {
      throw new UnableToLoadFileException("Failed to load file " + path, e);
    }
  }
}
