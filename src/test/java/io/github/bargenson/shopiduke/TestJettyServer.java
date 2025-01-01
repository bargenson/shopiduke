package io.github.bargenson.shopiduke;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.bargenson.shopiduke.auth.servlet.AuthFilter;
import io.github.bargenson.shopiduke.auth.servlet.AuthServlet;
import io.github.bargenson.shopiduke.auth.store.AccessTokenStore;
import io.github.bargenson.shopiduke.auth.store.InMemoryAccessTokenStore;
import io.github.bargenson.shopiduke.graphql.GraphQLClient;
import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class TestJettyServer {

  private static final String AUTH_PATH = "/auth";

  public void start() throws Exception {
    String baseUrl = startLocaltunnel();
    startServer(baseUrl);
  }

  public String startLocaltunnel() throws Exception {
    return new LocalTunnel("shopiduke").start();
  }

  private void startServer(String baseUrl) throws Exception {
    Properties properties = loadShopifyAppProperties();

    Server server = new Server(8080);
    AccessTokenStore accessTokenStore = new InMemoryAccessTokenStore();
    Servlet servlet =
        new AuthServlet(
            properties.getProperty("shopify.client.id"),
            properties.getProperty("shopify.client.secret"),
            properties.getProperty("shopify.client.scopes"),
            baseUrl + AUTH_PATH,
            accessTokenStore);

    ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
    handler.setContextPath("/");
    handler.addServlet(new ServletHolder(servlet), AUTH_PATH);
    handler.addFilter(
        new FilterHolder(new AuthFilter(accessTokenStore, baseUrl + AUTH_PATH)), "/*", null);
    handler.addServlet(new ServletHolder(new ListProductServlet(accessTokenStore)), "/");

    server.setHandler(handler);
    server.start();
  }

  private Properties loadShopifyAppProperties() throws IOException {
    Properties properties = new Properties();
    properties.load(getClass().getClassLoader().getResourceAsStream("shopifyapp.properties"));
    return properties;
  }

  public static void main(String[] args) throws Exception {
    new TestJettyServer().start();
  }

  private static class ListProductServlet extends HttpServlet {

    private final AccessTokenStore accessTokenStore;

    public ListProductServlet(AccessTokenStore accessTokenStore) {
      this.accessTokenStore = accessTokenStore;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      String shop = req.getParameter("shop");
      GraphQLClient client = new GraphQLClient(shop, accessTokenStore.get(shop), "2024-10");
      String query = "{ products(first: 10) { edges { node { title } } } }";
      JsonNode result = client.execute(query, null);
      resp.setContentType("text/plain");
      resp.getWriter().write(result.toString());
    }
  }
}
