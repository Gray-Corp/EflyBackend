package com.EFlyer.Bookings.YpsilonApiDocs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;


@Configuration
public class FlightApiConfig implements WebMvcConfigurer {

    @Bean
    public WebClient webClient(WebClient.Builder builder){
        HttpClient httpClient = HttpClient.create().protocol(HttpProtocol.HTTP11).responseTimeout(Duration.ofSeconds(20));
        return builder.clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type","application/xml")
                .defaultHeader("accept","application/xml")
                .build();
    }

}
