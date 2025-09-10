package com.weddingfit.service.finlife;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service @RequiredArgsConstructor
public class FinlifeClient {

  private final WebClient finlifeWebClient;
  @Value("${external.finlife.api-key}") private String apiKey;

  public String getRaw(String path, int pageNo) {
  return finlifeWebClient.get()
      .uri(u -> u.path(path)
          .queryParam("auth", apiKey)
          .queryParam("topFinGrpNo", "020000")
          .queryParam("pageNo", pageNo)
          .queryParam("pageSize", 500)
          .build())
      .retrieve()
      .onStatus(HttpStatusCode::isError, resp ->
          resp.bodyToMono(String.class).defaultIfEmpty("")
              .flatMap(b -> Mono.error(new IllegalStateException(
                  "[Finlife] " + resp.statusCode() + " " + b))))
      .bodyToMono(String.class)
      .block();
}

  public JsonNode get(String path, int pageNo) {
    return finlifeWebClient.get()
        .uri(u -> u.path(path)
            .queryParam("auth", apiKey)
                .queryParam("topFinGrpNo", "020000")
            .queryParam("pageNo", pageNo)
            .queryParam("pageSize", 500)
            .build())
        .retrieve()
        .onStatus(HttpStatusCode::isError, resp ->
            resp.bodyToMono(String.class).defaultIfEmpty("")
                .flatMap(b -> Mono.error(new IllegalStateException(
                    "[Finlife] " + resp.statusCode() + " " + b))))
        .bodyToMono(JsonNode.class)
        .block();
  }
}