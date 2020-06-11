/*
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.submission.verification;

import javax.net.ssl.HostnameVerifier;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("ssl-client-verification-verify-hostname")
public class DefaultHostnameVerifierProvider implements HostnameVerifierProvider {

  @Override
  public HostnameVerifier createHostnameVerifier() {
    return new DefaultHostnameVerifier();
  }
}
