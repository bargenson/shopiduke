package com.shopiduke.auth.store;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InMemoryAccessTokenStoreTest {

  private InMemoryAccessTokenStore store;

  @BeforeEach
  public void setUp() {
    store = new InMemoryAccessTokenStore();
  }

  @Test
  public void testSaveAndGet() {
    String shop = "test-shop.myshopify.com";
    String accessToken = "test-access-token";

    store.save(shop, accessToken);
    String retrievedToken = store.get(shop);

    assertEquals(
        accessToken, retrievedToken, "The retrieved access token should match the saved token.");
  }

  @Test
  public void testGetNonExistentShop() {
    String shop = "non-existent-shop.myshopify.com";

    String retrievedToken = store.get(shop);

    assertNull(
        retrievedToken, "The retrieved access token for a non-existent shop should be null.");
  }

  @Test
  public void testOverwriteAccessToken() {
    String shop = "test-shop.myshopify.com";
    String accessToken1 = "test-access-token-1";
    String accessToken2 = "test-access-token-2";

    store.save(shop, accessToken1);
    store.save(shop, accessToken2);
    String retrievedToken = store.get(shop);

    assertEquals(
        accessToken2,
        retrievedToken,
        "The retrieved access token should match the latest saved token.");
  }
}
