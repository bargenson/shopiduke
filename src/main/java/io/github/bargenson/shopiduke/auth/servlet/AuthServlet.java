package io.github.bargenson.shopiduke.auth.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bargenson.shopiduke.auth.HmacValidator;
import io.github.bargenson.shopiduke.auth.servlet.AuthFailureHandler.FailureReason;
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
  private final HmacValidator hmacValidator;
  private AuthSuccessHandler successHandler;
  private AuthFailureHandler failureHandler;

  public AuthServlet(
      String apiKey,
      String apiSecret,
      String scopes,
      AuthSuccessHandler successHandler,
      AuthFailureHandler failureHandler) {
    this.apiKey = apiKey;
    this.apiSecret = apiSecret;
    this.scopes = scopes;
    this.hmacValidator = new HmacValidator(apiSecret);
    this.successHandler = successHandler;
    this.failureHandler = failureHandler;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String shop = req.getParameter("shop");
    String code = req.getParameter("code");
    AuthRequest authRequest = new AuthRequest(shop, code, scopes);

    if (!authRequest.isValid()) {
      failureHandler.handle(req, resp, FailureReason.INVALID_REQUEST);
      return;
    }

    if (!authRequest.isAuthorizationGranted()) {
      String authorizationUrl =
          authRequest.buildAuthorizationUrl(apiKey, req.getRequestURL().toString());
      resp.sendRedirect(authorizationUrl);
      return;
    }

    if (!hmacValidator.validateHmac(req)) {
      failureHandler.handle(req, resp, FailureReason.INVALID_HMAC);
      return;
    }

    try {
      String accessToken = exchangeCodeForAccessToken(authRequest);
      successHandler.handle(req, resp, shop, accessToken);
    } catch (IOException | InterruptedException e) {
      failureHandler.handle(req, resp, FailureReason.CODE_EXCHANGE_FAILED);
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
    return parseAccessToken(response.body());
  }

  private String parseAccessToken(String jsonResponse) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode rootNode = objectMapper.readTree(jsonResponse);
    return rootNode.path("access_token").asText();
  }
}
