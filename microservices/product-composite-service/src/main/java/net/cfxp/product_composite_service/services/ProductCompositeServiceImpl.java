package net.cfxp.product_composite_service.services;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import net.cfxp.api.composite.product.ProductAggregate;
import net.cfxp.api.composite.product.ProductCompositeService;
import net.cfxp.api.composite.product.RecommendationSummary;
import net.cfxp.api.composite.product.ReviewSummary;
import net.cfxp.api.composite.product.ServiceAddresses;
import net.cfxp.api.core.product.Product;
import net.cfxp.api.core.recommendation.Recommendation;
import net.cfxp.api.core.review.Review;
import net.cfxp.api.util.http.ServiceUtil;
import reactor.core.publisher.Mono;;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

        private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);
        private final ServiceUtil serviceUtil;
        private ProductCompositeIntegration integration;

        // @Autowired
        public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
                this.serviceUtil = serviceUtil;
                this.integration = integration;
        }

        @Override
        public Mono<Void> createProduct(ProductAggregate body) {
                List<Mono> monoList = new ArrayList<>();

                LOG.debug("createCompositeProduct: creates a new composite entity for productId: {}",
                                body.getProductId());

                Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
                monoList.add(integration.createProduct(product));

                if (body.getRecommendations() != null) {
                        body.getRecommendations().forEach(r -> {
                                Recommendation recommendation = new Recommendation(body.getProductId(),
                                                r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(),
                                                null);
                                monoList.add(integration.createRecommendation(recommendation));
                        });

                }
                if (body.getReviews() != null) {
                        body.getReviews().forEach(r -> {
                                Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(),
                                                r.getSubject(), r.getContent(), null);
                                monoList.add(integration.createReview(review));
                        });
                }
                LOG.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());

                return Mono.zip(r -> "", monoList.toArray(new Mono[0]))
                                .doOnError(ex -> LOG.warn("createCompositeProduct failed: ", ex.toString())).then();
        }

        @Override
        public Mono<ProductAggregate> getProduct(int productId) {
                LOG.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId);

                return Mono.zip(values -> createProductAggregate((Product) values[0], (List<Recommendation>) values[1],
                                (List<Review>) values[2], serviceUtil.getServiceAddress()),
                                integration.getProduct(productId),
                                integration.getRecommendations(productId).collectList(),
                                integration.getReviews(productId).collectList())
                                .doOnError(ex -> LOG.warn("getCompositeProuct failed: {}", ex.toString()))
                                .log(LOG.getName(), Level.FINE);
        }

        @Override
        public Mono<Void> deleteProduct(int productId) {
                LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

                return Mono.zip(r -> "", integration.deleteProduct(productId),
                                integration.deleteRecommendation(productId), integration.deleteReviews(productId))
                                .doOnError(ex -> LOG.warn("delete fialed: {}", ex.toString()))
                                .log(LOG.getName(), Level.FINE).then();

                // LOG.debug("deleteCompositeProduct: aggregate entities deleted for productId:
                // {}", productId);
        }

        private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations,
                        List<Review> reviews, String serviceAddress) {

                int productId = product.getProductId();
                String name = product.getName();
                int weight = product.getWeight();

                List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null
                                : recommendations.stream()
                                                .map(r -> new RecommendationSummary(r.getRecommendationId(),
                                                                r.getAuthor(), r.getRate()))
                                                .collect(Collectors.toList());

                List<ReviewSummary> reviewSummaries = (reviews == null) ? null
                                : reviews.stream()
                                                .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(),
                                                                r.getSubject(), r.getContent()))
                                                .collect(Collectors.toList());

                String productAddress = product.getServiceAddress();
                String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress()
                                : "";
                String recommendationAddress = (recommendations != null && recommendations.size() > 0)
                                ? recommendations.get(0).getServiceAddress()
                                : "";
                ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress,
                                recommendationAddress);

                return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries,
                                serviceAddresses);
        }
}
