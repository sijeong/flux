package net.cfxp.product_composite_service.services;

import java.util.List;
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
import net.cfxp.api.exceptions.NotFoundException;
import net.cfxp.api.util.http.ServiceUtil;;

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
        public void createProduct(ProductAggregate body) {
                try {
                        LOG.debug("createCompositeProduct: creates a new composite entity for productId: {}",
                                        body.getProductId());

                        Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
                        integration.createProduct(product);

                        if (body.getRecommendations() != null) {
                                body.getRecommendations().forEach(r -> {
                                        Recommendation recommendation = new Recommendation(body.getProductId(),
                                                        r.getRecommendationId(), r.getAuthor(), r.getRate(),
                                                        r.getContent(), null);
                                        integration.createRecommendation(recommendation);
                                });

                        }
                        if (body.getReviews() != null) {
                                body.getReviews().forEach(r -> {
                                        Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(),
                                                        r.getSubject(), r.getContent(), null);
                                        integration.createReview(review);
                                });
                        }
                        LOG.debug("createCompositeProduct: composite entities created for productId: {}",
                                        body.getProductId());
                } catch (RuntimeException re) {
                        LOG.warn("createCompositeProduct fialed:", re);
                        throw re;
                }
        }

        @Override
        public ProductAggregate getProduct(int productId) {
                LOG.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId);

                Product product = integration.getProduct(productId);
                if (product == null) {
                        throw new NotFoundException("No product found for productId: " + productId);
                }

                List<Recommendation> recommendations = integration.getRecommendations(productId);
                List<Review> reviews = integration.getReviews(productId);

                LOG.debug("getCompositeProduct: aggregate entity found for productId: {}", productId);

                return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
        }

        @Override
        public void deleteProduct(int productId) {
                LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

                integration.deleteProduct(productId);
                integration.deleteRecommendation(productId);
                integration.deleteReviews(productId);

                LOG.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);
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
