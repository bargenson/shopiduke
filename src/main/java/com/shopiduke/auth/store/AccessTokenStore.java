package com.shopiduke.auth.store;

public interface AccessTokenStore {
  String get(String shop);

  void save(String shop, String accessToken);
}
