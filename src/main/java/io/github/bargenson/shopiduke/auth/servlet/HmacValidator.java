package io.github.bargenson.shopiduke.auth.servlet;

import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacValidator implements Serializable {

  private final String apiSecret;

  public HmacValidator(String apiSecret) {
    this.apiSecret = apiSecret;
  }

  public boolean validateHmac(HttpServletRequest req) {
    try {
      Map<String, String> params = new TreeMap<>();
      req.getParameterMap()
          .forEach(
              (key, values) -> {
                if (!"hmac".equals(key)) {
                  params.put(key, values[0]);
                }
              });

      StringBuilder data = new StringBuilder();
      for (Map.Entry<String, String> entry : params.entrySet()) {
        if (data.length() > 0) {
          data.append("&");
        }
        data.append(entry.getKey()).append("=").append(entry.getValue());
      }

      Mac mac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKeySpec =
          new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
      mac.init(secretKeySpec);
      byte[] hash = mac.doFinal(data.toString().getBytes(StandardCharsets.UTF_8));

      String expectedHmac = bytesToHex(hash);
      String providedHmac = req.getParameter("hmac");
      return providedHmac != null && providedHmac.equals(expectedHmac);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  private String bytesToHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : bytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }
}
