package io.github.bargenson.shopiduke.auth.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

@FunctionalInterface
public interface AuthFailureHandler extends Serializable {

  public enum FailureReason {
    INVALID_REQUEST,
    INVALID_HMAC,
    CODE_EXCHANGE_FAILED
  }

  void handle(HttpServletRequest request, HttpServletResponse response, FailureReason reason)
      throws IOException;
}
