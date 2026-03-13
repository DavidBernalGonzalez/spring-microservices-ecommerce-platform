package com.microservice.inventory.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.product.base-url}") String productBaseUrl) {
        this.restClient = restClientBuilder
                .baseUrl(productBaseUrl)
                .build();
    }

    public void validateProductExists(Long productId) {
        try {
            restClient.get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND,
                                "Product not found for productId: " + productId);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        throw new ResponseStatusException(
                                org.springframework.http.HttpStatus.BAD_GATEWAY,
                                "Product service is unavailable");
                    })
                    .toBodilessEntity();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "Error connecting to product service");
        }
    }
}