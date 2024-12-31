package com.shopiduke;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class LocalTunnel {
  private final String subdomainPrefix;

  public LocalTunnel(String subdomainPrefix) {
    this.subdomainPrefix = subdomainPrefix;
  }

  public String start() throws IOException {
    String subdomain = String.format("%s-%s", subdomainPrefix, getLocalIP().replaceAll("\\.", "-"));
    ProcessBuilder processBuilder =
        new ProcessBuilder("lt", "--port", "8080", "--subdomain", subdomain);
    processBuilder.start();
    String localtunnelUrl = String.format("https://%s.loca.lt", subdomain);
    System.out.println(String.format("Localtunnel URL: %s", localtunnelUrl));
    return localtunnelUrl;
  }

  private String getLocalIP() throws IOException {
    String checkIpServiceUrl = "http://checkip.amazonaws.com/";
    URL url = new URL(checkIpServiceUrl);
    try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
      return br.readLine();
    }
  }
}
