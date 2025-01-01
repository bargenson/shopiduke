package io.github.bargenson.shopiduke.auth.store;

import java.util.HashMap;
import java.util.Map;

public class InMemoryAccessTokenStore implements AccessTokenStore {
  private static final Map<String, String> store = new HashMap<>();

  @Override
  public String get(String shop) {
    return store.get(shop);
  }

  @Override
  public void save(String shop, String accessToken) {
    store.put(shop, accessToken);
  }
}
