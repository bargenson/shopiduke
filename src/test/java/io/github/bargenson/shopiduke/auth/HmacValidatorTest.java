package io.github.bargenson.shopiduke.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class HmacValidatorTest {

  private static final String API_SECRET = "test-secret";

  private HmacValidator hmacValidator;

  @BeforeEach
  public void setUp() {
    hmacValidator = new HmacValidator(API_SECRET);
  }

  @Test
  public void validateHmac_returnsTrue_whenHmacIsValid() {
    // Given
    final HttpServletRequest req =
        MockMvcRequestBuilders.get("/")
            .queryParam("param1", "value1")
            .queryParam("param2", "value2")
            .queryParam("param3", "value3")
            .queryParam("hmac", "f5d221a3d3d23fcf49bcd1c8931c433f04c29b5aba5724ee50e49b98bae8e21e")
            .buildRequest(null);

    // When
    final boolean result = hmacValidator.validateHmac(req);

    // Then
    assertTrue(result);
  }

  @Test
  public void validateHmac_returnsFalse_whenHmacIsMissing() {
    // Given
    final HttpServletRequest req =
        MockMvcRequestBuilders.get("/")
            .queryParam("param1", "value1")
            .queryParam("param2", "value2")
            .queryParam("param3", "value3")
            .buildRequest(null);

    // When
    final boolean result = hmacValidator.validateHmac(req);

    // Then
    assertFalse(result);
  }

  @Test
  public void validateHmac_returnsTrue_whenHmacIsInvalid() {
    // Given
    final HttpServletRequest req =
        MockMvcRequestBuilders.get("/")
            .queryParam("param1", "value1")
            .queryParam("param2", "value2")
            .queryParam("param3", "value3")
            .queryParam("hmac", "wrong-hmac")
            .buildRequest(null);

    // When
    final boolean result = hmacValidator.validateHmac(req);

    // Then
    assertFalse(result);
  }
}
