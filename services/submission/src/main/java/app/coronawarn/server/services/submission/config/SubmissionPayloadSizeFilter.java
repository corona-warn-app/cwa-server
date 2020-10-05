

package app.coronawarn.server.services.submission.config;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * {@link SubmissionPayloadSizeFilter} instances filter requests exceeding a certain size limit.
 */
@Component
public class SubmissionPayloadSizeFilter extends OncePerRequestFilter {

  private final long maximumRequestSize;

  public SubmissionPayloadSizeFilter(SubmissionServiceConfig config) {
    this.maximumRequestSize = config.getMaximumRequestSize().toBytes();
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
