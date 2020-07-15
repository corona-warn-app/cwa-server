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

  private static final int MAX_REQUEST_SIZE = 100 * 1024;

  /**
   * Filters each request that exceeds the maximum size of 100KB
   *
   * @param request
   * @param response
   * @param filterChain
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (request.getContentLengthLong() > MAX_REQUEST_SIZE) {
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      response.getWriter().write("Request size exceeded limit of " + MAX_REQUEST_SIZE + " bytes");
    } else {
      filterChain.doFilter(request, response);
    }
  }

}
