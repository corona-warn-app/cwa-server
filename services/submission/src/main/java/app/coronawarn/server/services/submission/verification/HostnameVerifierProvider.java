

package app.coronawarn.server.services.submission.verification;

import javax.net.ssl.HostnameVerifier;

public interface HostnameVerifierProvider {

  HostnameVerifier createHostnameVerifier();
}
