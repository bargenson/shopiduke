package io.github.bargenson.shopiduke.auth.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

@FunctionalInterface
public interface AuthSuccessHandler extends Serializable {
  void handle(
      HttpServletRequest request, HttpServletResponse response, String shop, String accessToken)
      throws IOException;
}
