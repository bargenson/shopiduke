package io.github.bargenson.shopiduke;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.bargenson.shopiduke.auth.servlet.AuthFilter;
import io.github.bargenson.shopiduke.auth.servlet.AuthServlet;
import io.github.bargenson.shopiduke.graphql.GraphQLClient;
import io.github.bargenson.shopiduke.utils.LocalTunnel;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class TestSpringBoot {

  private static final String AUTH_SERVLET_PATH = "/auth";

  public static void main(String[] args) throws Exception {
    new LocalTunnel("shopiduke").start();
    SpringApplication.run(TestSpringBoot.class, args);
  }

  @Bean
  public Map<String, String> accessTokens() {
    return new HashMap<>();
  }

  @Bean
  public ServletRegistrationBean<AuthServlet> authServlet(Map<String, String> accessTokens) {
    Properties properties = loadShopifyAppProperties();
    AuthServlet authServlet =
        new AuthServlet(
            properties.getProperty("shopify.client.id"),
            properties.getProperty("shopify.client.secret"),
            properties.getProperty("shopify.client.scopes"),
            (req, resp, shop, accessToken) -> {
              accessTokens.put(shop, accessToken);
              resp.sendRedirect("/?shop=" + shop);
            },
            (req, resp, reason) -> {
              resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
              resp.getWriter().write("Access denied: " + reason);
            });
    return new ServletRegistrationBean<>(authServlet, AUTH_SERVLET_PATH);
  }

  @Bean
  public FilterRegistrationBean<AuthFilter> authFilter(Map<String, String> accessTokens)
      throws URISyntaxException {
    AuthFilter authFilter = new AuthFilter((shop) -> accessTokens.get(shop), AUTH_SERVLET_PATH);
    FilterRegistrationBean<AuthFilter> registrationBean = new FilterRegistrationBean<>(authFilter);
    registrationBean.addUrlPatterns("/*");
    return registrationBean;
  }

  private Properties loadShopifyAppProperties() {
    Properties properties = new Properties();
    try {
      properties.load(getClass().getClassLoader().getResourceAsStream("shopifyapp.properties"));
    } catch (IOException e) {
      throw new RuntimeException("Failed to load shopifyapp.properties", e);
    }
    return properties;
  }

  @RestController
  @RequestMapping("/")
  public static class ProductController {
    private final Map<String, String> accessTokens;

    @Autowired
    public ProductController(Map<String, String> accessTokens) {
      this.accessTokens = accessTokens;
    }

    @GetMapping
    public JsonNode listProducts(@RequestParam(name = "shop") String shop) throws IOException {
      GraphQLClient client = new GraphQLClient(shop, accessTokens.get(shop), "2024-10");
      String query = "{ products(first: 10) { edges { node { title } } } }";
      JsonNode result = client.execute(query, null);
      return result;
    }
  }
}
