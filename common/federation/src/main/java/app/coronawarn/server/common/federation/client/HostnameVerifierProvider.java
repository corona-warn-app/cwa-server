package app.coronawarn.server.common.federation.client;

import javax.net.ssl.HostnameVerifier;

public interface HostnameVerifierProvider {

  HostnameVerifier createHostnameVerifier();
}
