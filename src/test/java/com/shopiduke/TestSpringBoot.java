package com.shopiduke;

import com.fasterxml.jackson.databind.JsonNode;
import com.shopiduke.auth.servlet.AuthFilter;
import com.shopiduke.auth.servlet.AuthServlet;
import com.shopiduke.auth.store.AccessTokenStore;
import com.shopiduke.auth.store.InMemoryAccessTokenStore;
import com.shopiduke.graphql.GraphQLClient;
import java.io.IOException;
import java.net.URISyntaxException;
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

  public static void main(String[] args) throws Exception {
    SpringApplication.run(TestSpringBoot.class, args);
  }

  @Bean
  public String baseUrl() throws Exception {
    return new LocalTunnel("shopiduke").start();
  }

  @Bean
  public AccessTokenStore accessTokenStore() {
    return new InMemoryAccessTokenStore();
  }

  @Bean
  public ServletRegistrationBean<AuthServlet> authServlet(
      AccessTokenStore accessTokenStore, String baseUrl) {
    Properties properties = loadShopifyAppProperties();
    AuthServlet authServlet =
        new AuthServlet(
            properties.getProperty("shopify.client.id"),
            properties.getProperty("shopify.client.secret"),
            properties.getProperty("shopify.client.scopes"),
            baseUrl + "/auth",
            accessTokenStore);
    return new ServletRegistrationBean<>(authServlet, "/auth");
  }

  @Bean
  public FilterRegistrationBean<AuthFilter> authFilter(
      AccessTokenStore accessTokenStore, String baseUrl) throws URISyntaxException {
    AuthFilter authFilter = new AuthFilter(accessTokenStore, baseUrl + "/auth");
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
    private final AccessTokenStore accessTokenStore;

    @Autowired
    public ProductController(AccessTokenStore accessTokenStore) {
      this.accessTokenStore = accessTokenStore;
    }

    @GetMapping
    public JsonNode listProducts(@RequestParam(name = "shop") String shop) throws IOException {
      GraphQLClient client = new GraphQLClient(shop, accessTokenStore.get(shop), "2024-10");
      String query = "{ products(first: 10) { edges { node { title } } } }";
      JsonNode result = client.execute(query, null);
      return result;
    }
  }
}
