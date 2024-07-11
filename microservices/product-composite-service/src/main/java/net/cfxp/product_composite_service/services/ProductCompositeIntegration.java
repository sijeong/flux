package net.cfxp.product_composite_service.services;

import java.io.IOException;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.cfxp.api.core.product.Product;
import net.cfxp.api.core.product.ProductService;
import net.cfxp.api.core.recommendation.Recommendation;
import net.cfxp.api.core.recommendation.RecommendationService;
import net.cfxp.api.core.review.Review;
import net.cfxp.api.core.review.ReviewService;
import net.cfxp.api.exceptions.InvalidInputException;
import net.cfxp.api.exceptions.NotFoundException;
import net.cfxp.api.util.http.HttpErrorInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    // private final RestTemplate restTemplate;
    private final WebClient webClient;
    private final ObjectMapper mapper;

    private static final String PRODUCT_SERVICE_URL = "http://product";
    private static final String RECOMMENDATION_SERVICE_URL = "http://recommendation";
    private static final String REVIEW_SERVICE_URL = "http://review";

    @Autowired
    public ProductCompositeIntegration(WebClient.Builder webClientBuilder, ObjectMapper mapper
    // @Value("${app.product-service.host}") String productServiceHost,
    // @Value("${app.product-service.port}") int productSercvicePort,
    // @Value("${app.recommendation-service.host}") String
    // recommendationServiceHost,
    // @Value("${app.recommendation-service.port}") int recommendationServicePort,
    // @Value("${app.review-service.host}") String reviewServiceHost,
    // @Value("${app.review-service.port}") int reviewServicePort
    ) {
        this.webClient = webClientBuilder.build();
        this.mapper = mapper;

        // this.productServiceUrl = "http://" + productServiceHost + ":" +
        // productSercvicePort + "/product/";
        // this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" +
        // recommendationServicePort
        // + "/recommendation?productId=";
        // this.reviewServiceUrl = "http://" + reviewServiceHost + ":" +
        // reviewServicePort + "/review?productId=";
    }

    @Override
    public Mono<Product> createProduct(Product body) {

        String url = PRODUCT_SERVICE_URL + "/product/";

        return webClient.post().uri(url).retrieve().bodyToMono(Product.class).log(LOG.getName(), Level.FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public Mono<Product> getProduct(int productId) {

        String url = PRODUCT_SERVICE_URL + "/product/" + productId;

        return webClient.get().uri(url).retrieve().bodyToMono(Product.class).log(LOG.getName(), Level.FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {

        String url = PRODUCT_SERVICE_URL + "/product/" + productId;

        return webClient.delete().uri(url).retrieve().bodyToMono(Void.class).log(LOG.getName(), Level.FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {

        String url = RECOMMENDATION_SERVICE_URL + "/recommendation";

        return webClient.post().uri(url).retrieve().bodyToMono(Recommendation.class).log(LOG.getName(), Level.FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {

        String url = RECOMMENDATION_SERVICE_URL + "/recommendation?productId=" + productId;

        return webClient.get().uri(url).retrieve().bodyToFlux(Recommendation.class).log(LOG.getName(), Level.FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public Mono<Void> deleteRecommendation(int productId) {

        String url = RECOMMENDATION_SERVICE_URL + "/recommendation?productId=" + productId;

        return webClient.delete().uri(url).retrieve().bodyToMono(Void.class).log(LOG.getName(), Level.FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public Mono<Review> createReview(Review body) {

        String url = REVIEW_SERVICE_URL + "/review";

        return webClient.post().uri(url).retrieve().bodyToMono(Review.class).log(LOG.getName(), Level.FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public Flux<Review> getReviews(int productId) {

        String url = REVIEW_SERVICE_URL + "/review?productId=" + productId;

        return webClient.delete().uri(url).retrieve().bodyToFlux(Review.class).log(LOG.getName(), Level.FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {

        String url = REVIEW_SERVICE_URL + "/review?productId" + productId;

        return webClient.delete().uri(url).retrieve().bodyToMono(Void.class).log(LOG.getName(), Level.FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

    private Throwable handleException(Throwable ex) {
        if (!(ex instanceof WebClientResponseException)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException) ex;

        switch (HttpStatus.resolve(wcre.getStatusCode().value())) {
        case NOT_FOUND:
            return new NotFoundException(getErrorMessage(wcre));
        case UNPROCESSABLE_ENTITY:
            return new InvalidInputException(getErrorMessage(wcre));
        default:
            LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
            LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
            return ex;
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        switch (HttpStatus.resolve(ex.getStatusCode().value())) {
        case NOT_FOUND:
            return new NotFoundException(getErrorMessage(ex));
        case UNPROCESSABLE_ENTITY:
            return new InvalidInputException(getErrorMessage(ex));
        default:
            LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
            LOG.warn("Error body: {}", ex.getResponseBodyAsString());
            return ex;
        }
    }
}
