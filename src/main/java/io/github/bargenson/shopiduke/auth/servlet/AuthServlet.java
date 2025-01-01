package io.github.bargenson.shopiduke.auth.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bargenson.shopiduke.auth.store.AccessTokenStore;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthServlet extends HttpServlet {

  private final String apiKey;
  private final String apiSecret;
  private final String scopes;
  private final String redirectUri;
  private final HmacValidator hmacValidator;
  private final AccessTokenStore store;

  public AuthServlet(
      String apiKey, String apiSecret, String scopes, String redirectUri, AccessTokenStore store) {
    this.apiKey = apiKey;
    this.apiSecret = apiSecret;
    this.scopes = scopes;
    this.redirectUri = redirectUri;
    this.hmacValidator = new HmacValidator(apiSecret);
    this.store = store;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String shop = req.getParameter("shop");
    String code = req.getParameter("code");
    AuthRequest authRequest = new AuthRequest(shop, code, scopes);

    if (!authRequest.isValid()) {
      resp.setStatus(400);
      resp.getWriter().write("Invalid request");
      return;
    }

    if (!authRequest.isAuthorizationGranted()) {
      String oauthUrl = authRequest.buildAuthorizationUrl(apiKey, redirectUri);
      resp.sendRedirect(oauthUrl);
      return;
    }

    if (!hmacValidator.validateHmac(req)) {
      resp.setStatus(400);
      resp.getWriter().write("Invalid HMAC");
      return;
    }

    try {
      String accessToken = exchangeCodeForAccessToken(authRequest);
      resp.getWriter().write("Access Token: " + accessToken);
    } catch (IOException | InterruptedException e) {
      resp.setStatus(500);
      resp.getWriter().write("Error exchanging code for access token");
    }
  }

  private String exchangeCodeForAccessToken(AuthRequest authRequest)
      throws IOException, InterruptedException {
    String tokenEndpoint =
        String.format("https://%s/admin/oauth/access_token", authRequest.getShop());
    String requestBody =
        String.format(
            "client_id=%s&client_secret=%s&code=%s", apiKey, apiSecret, authRequest.getCode());

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(java.net.URI.create(tokenEndpoint))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    String accessToken = parseAccessToken(response.body());
    store.save(authRequest.getShop(), accessToken);
    return accessToken;
  }

  private String parseAccessToken(String jsonResponse) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode rootNode = objectMapper.readTree(jsonResponse);
    return rootNode.path("access_token").asText();
  }
}
