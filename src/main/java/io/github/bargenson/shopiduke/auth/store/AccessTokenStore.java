package io.github.bargenson.shopiduke.auth.store;

import java.io.Serializable;

@FunctionalInterface
public interface AccessTokenStore extends Serializable {
  String get(String shop);
}
