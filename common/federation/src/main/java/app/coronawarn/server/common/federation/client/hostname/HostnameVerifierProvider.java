package app.coronawarn.server.common.federation.client.hostname;

import javax.net.ssl.HostnameVerifier;

public interface HostnameVerifierProvider {

  HostnameVerifier createHostnameVerifier();
}
