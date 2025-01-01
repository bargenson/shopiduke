package io.github.bargenson.shopiduke.auth.servlet;

import io.github.bargenson.shopiduke.auth.store.AccessTokenStore;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthFilter implements Filter {

  private final AccessTokenStore store;
  private final String authServletPath;

  public AuthFilter(AccessTokenStore store, String authServletPath) {
    this.store = store;
    this.authServletPath = authServletPath;
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

    if (store.get(shop) == null && !isAuthServletRequested(httpRequest)) {
      String authServletPathWithContext = httpRequest.getContextPath() + authServletPath;
      String redirectUrl = String.format("%s?shop=%s", authServletPathWithContext, shop);
      httpResponse.sendRedirect(redirectUrl);
      return;
    }

    chain.doFilter(request, response);
  }

  private boolean isAuthServletRequested(HttpServletRequest httpRequest) {
    return httpRequest.getServletPath().replace("?", "").equals(authServletPath);
  }

  @Override
  public void destroy() {}
}
