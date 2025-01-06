package io.github.bargenson.shopiduke;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.bargenson.shopiduke.auth.servlet.AuthFilter;
import io.github.bargenson.shopiduke.auth.servlet.AuthServlet;
import io.github.bargenson.shopiduke.graphql.GraphQLClient;
import io.github.bargenson.shopiduke.utils.LocalTunnel;
import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class TestJettyServer {

  private static final String AUTH_SERVLET_PATH = "/auth";

  private HashMap<String, String> accessTokens;

  public TestJettyServer() {
    this.accessTokens = new HashMap<>();
  }

  public void start() throws Exception {
    startLocaltunnel();
    startServer();
  }

  public String startLocaltunnel() throws Exception {
    return new LocalTunnel("shopiduke").start();
  }

  private void startServer() throws Exception {
    Properties properties = loadShopifyAppProperties();

    Server server = new Server(8080);
    Servlet servlet =
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

    ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
    handler.setContextPath("/");
    handler.addServlet(new ServletHolder(servlet), AUTH_SERVLET_PATH);
    handler.addFilter(
        new FilterHolder(new AuthFilter((shop) -> accessTokens.get(shop), AUTH_SERVLET_PATH)),
        "/*",
        null);
    handler.addServlet(new ServletHolder(new ListProductServlet(accessTokens)), "/");

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

    private final Map<String, String> accessTokens;

    public ListProductServlet(Map<String, String> accessTokens) {
      this.accessTokens = accessTokens;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      String shop = req.getParameter("shop");
      GraphQLClient client = new GraphQLClient(shop, accessTokens.get(shop), "2024-10");
      String query = "{ products(first: 10) { edges { node { title } } } }";
      JsonNode result = client.execute(query, null);
      resp.setContentType("application/json");
      resp.getWriter().write(result.toString());
    }
  }
}
