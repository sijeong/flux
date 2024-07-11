package net.cfxp.api.composite.product;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import reactor.core.publisher.Mono;

/**
 * ProductCompositeService
 */
public interface ProductCompositeService {

    @PostMapping(value = "/product-composite", produces = "application/json")
    Mono<Void> createProduct(@RequestBody ProductAggregate body);

    @GetMapping(value = "/product-composite/{productId}", produces = "application/json")
    Mono<ProductAggregate> getProduct(@PathVariable int productId);

    @DeleteMapping(value = "/product-composite/{productId}")
    Mono<Void> deleteProduct(@PathVariable int productId);
}