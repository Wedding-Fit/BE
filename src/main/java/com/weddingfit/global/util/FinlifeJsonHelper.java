package com.weddingfit.global.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class FinlifeJsonHelper {
  public String text(JsonNode n, String... keys) {
    if (n == null) return null;
    for (String k : keys) {
      JsonNode x = n.get(k);
      if (x != null && !x.isNull()) return x.asText();
    }
    return null;
  }
  public BigDecimal decimal(JsonNode n, String... keys) {
    String v = text(n, keys);
    if (v == null || v.isBlank()) return null;
    try { return new BigDecimal(v); } catch (Exception e) { return null; }
  }
}