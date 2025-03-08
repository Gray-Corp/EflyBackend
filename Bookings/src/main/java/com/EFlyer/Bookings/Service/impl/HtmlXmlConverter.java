package com.EFlyer.Bookings.Service.impl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class HtmlXmlConverter {
    private final WebClient webClient;

    // Constructor to initialize WebClient (assuming it's configured as a Bean in your Spring configuration)
    public HtmlXmlConverter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://stagingxml.ypsilon.net:11024/").build(); // Replace with your API URL
    }

    public Mono<String> sendPostAndConvertHtmlToXml(String requestBody) {
        // Fetch HTML content reactively using WebClient
        return webClient.post()
                .contentType(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .header("Accept-Encoding","gzip, deflate")
                .header("api-version","3.92")
                .header("accessmode","agency")
                .header("accessid","eflycha eflycha")
                .header("authmode","pwd")
                .header("session")
                .header("Connection","close")
                .header("Authorization","Basic ZWZseWNoOlFSWlRDdWM5OExfSUpDVmRZRGxrZnVXQVEyd0pXbSFE")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::convertHtmlToXml);  // Convert the HTML to XML
    }

    private String convertHtmlToXml(String htmlContent) {
        // Parse HTML using Jsoup
        Document document = Jsoup.parse(htmlContent);

        // Extract the XML part (for example, availResponse)
        Element availResponse = document.selectFirst("availResponse");

        // Prepend the XML declaration and return the XML content as a string
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + availResponse.outerHtml();
    }
}
