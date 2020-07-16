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

package app.coronawarn.server.services.submission.config;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SubmissionPayloadSizeFilter extends OncePerRequestFilter {

  private final Integer maximumRequestSize;

  public SubmissionPayloadSizeFilter(SubmissionServiceConfig config) {
    this.maximumRequestSize = config.getMaximumRequestSize();
  }

  /**
   * Filters each request that exceeds the maximum size.
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (request.getContentLengthLong() > maximumRequestSize) {
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      response.getWriter().write("Request size exceeded limit of " + maximumRequestSize + " bytes");
    } else {
      filterChain.doFilter(request, response);
    }
  }

}
