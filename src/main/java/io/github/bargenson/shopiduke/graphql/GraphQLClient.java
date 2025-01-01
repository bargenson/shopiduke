package io.github.bargenson.shopiduke.graphql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class GraphQLClient {
  private final URI endpoint;
  private final String accessToken;
  private final HttpClient client;

  public GraphQLClient(String shop, String accessToken, String version) {
    this.endpoint = URI.create("https://" + shop + "/admin/api/" + version + "/graphql.json");
    this.accessToken = accessToken;
    this.client = HttpClient.newHttpClient();
  }

  public JsonNode execute(String query, Map<String, Object> variables) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      ObjectNode requestBody = JsonNodeFactory.instance.objectNode();
      requestBody.put("query", query);
      requestBody.set("variables", objectMapper.valueToTree(variables));

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(endpoint)
              .header("Content-Type", "application/json")
              .header("X-Shopify-Access-Token", accessToken)
              .POST(
                  HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
              .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      return objectMapper.readTree(response.body());
    } catch (Exception e) {
      throw new RuntimeException("Error executing GraphQL query", e);
    }
  }

  public URI getEndpoint() {
    return endpoint;
  }

  public String getAccessToken() {
    return accessToken;
  }
}
