package io.github.bargenson.shopiduke.auth.store;

import java.io.Serializable;

public interface AccessTokenStore extends Serializable {
  String get(String shop);

  void save(String shop, String accessToken);
}
