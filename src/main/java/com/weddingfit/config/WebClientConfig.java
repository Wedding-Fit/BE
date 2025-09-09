package com.weddingfit.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

   @Bean
public WebClient finlifeWebClient(@Value("${external.finlife.base-url}") String baseUrl) {
  return WebClient.builder()
      .baseUrl(baseUrl)
      .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .clientConnector(new ReactorClientHttpConnector(
          HttpClient.create()
              .responseTimeout(Duration.ofSeconds(15))
              .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)))
      .build();
}
}
