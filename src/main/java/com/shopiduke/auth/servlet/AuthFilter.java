package com.shopiduke.auth.servlet;

import com.shopiduke.auth.store.AccessTokenStore;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AuthFilter implements Filter {

  private final AccessTokenStore store;
  private final URI authUri;

  public AuthFilter(AccessTokenStore store, String authUri) throws URISyntaxException {
    this.store = store;
    this.authUri = new URI(authUri);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String shop = httpRequest.getParameter("shop");
    if (shop == null) {
      httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
      httpResponse.getWriter().write("Access denied: Invalid or missing shop parameter");
      return;
    }

    if (store.get(shop) == null && !httpRequest.getRequestURI().equals(authUri.getPath())) {
      httpResponse.sendRedirect(authUri + "?shop=" + shop);
      return;
    }

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {}
}
