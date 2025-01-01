package io.github.bargenson.shopiduke.auth.servlet;

public class AuthRequest {
  private final String shop;
  private final String code;
  private final String scopes;

  public AuthRequest(String shop, String code, String scopes) {
    this.shop = shop;
    this.code = code;
    this.scopes = scopes;
  }

  public boolean isValid() {
    return shop != null;
  }

  public boolean isAuthorizationGranted() {
    return code != null;
  }

  public String buildAuthorizationUrl(String apiKey, String redirectUri) {
    return String.format(
        "https://%s/admin/oauth/authorize?client_id=%s&scope=%s&redirect_uri=%s",
        shop, apiKey, scopes, redirectUri);
  }

  public String getShop() {
    return shop;
  }

  public String getCode() {
    return code;
  }
}
