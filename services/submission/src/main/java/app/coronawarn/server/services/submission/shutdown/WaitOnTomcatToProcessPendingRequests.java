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

package app.coronawarn.server.services.submission.shutdown;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

public class WaitOnTomcatToProcessPendingRequests
    implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {

  private static final Logger logger = LoggerFactory.getLogger(WaitOnTomcatToProcessPendingRequests.class);

  private volatile Connector connector;

  @Override
  public void onApplicationEvent(ContextClosedEvent event) {
    logger.info("receiced ContextClosedEvent '{}', pausing the connector...", event);
    this.connector.pause();
    Executor executor = this.connector.getProtocolHandler().getExecutor();
    if (executor instanceof ThreadPoolExecutor) {
      try {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
        threadPoolExecutor.shutdown();
        if (!threadPoolExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
          logger.warn("Tomcat thread pool did not shut down gracefully within "
              + "30 seconds. Proceeding with forceful shutdown");
        }
      } catch (InterruptedException e) {
        logger.debug(e.getLocalizedMessage(), e);
        Thread.currentThread().interrupt();
      }
    }
  }

  @Override
  public void customize(Connector connector) {
    this.connector = connector;
  }
}
