package net.cfxp.microservices.composite.product;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import net.cfxp.api.composite.product.ProductAggregate;
import net.cfxp.api.composite.product.RecommendationSummary;
import net.cfxp.api.composite.product.ReviewSummary;
import net.cfxp.api.core.product.Product;
import net.cfxp.api.core.recommendation.Recommendation;
import net.cfxp.api.core.review.Review;
import net.cfxp.api.exceptions.InvalidInputException;
import net.cfxp.api.exceptions.NotFoundException;
import net.cfxp.microservices.composite.product.services.ProductCompositeIntegration;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ProductCompositeServiceApplicationTests {

	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 2;
	private static final int PRODUCT_ID_INVALID = 3;

	@Autowired
	private WebTestClient client;
	@MockBean
	private ProductCompositeIntegration compositeIntegration;

	@BeforeEach
	void setUp() {
		when(compositeIntegration.getProduct(PRODUCT_ID_OK))
				.thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-address"));
		when(compositeIntegration.getRecommendations(PRODUCT_ID_OK)).thenReturn(
				singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address")));
		when(compositeIntegration.getReviews(PRODUCT_ID_OK))
				.thenReturn(singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subjet", "content", "mock address")));

		when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
				.thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));
		when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
				.thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
	}

	@Test
	void contextLoads() {
	}

	@Test
	void createCompositeProduct1() {
		ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1, null, null, null);
		postAndVerifyProduct(compositeProduct, HttpStatus.OK);
	}

	@Test
	void createCompositeProduct2() {
		ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
				singletonList(new RecommendationSummary(1, "author", 1, "content")),
				singletonList(new ReviewSummary(1, "author", "subject", "content")), null);
		postAndVerifyProduct(compositeProduct, HttpStatus.OK);
	}

	@Test
	void getProductById() {
		getAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
		.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
		.jsonPath("$.recommendations.length()").isEqualTo(1)
		.jsonPath("$.reviews.length()").isEqualTo(1);
	}

	private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus) {
		client.post().uri("/product-composite").body(Mono.just(compositeProduct), ProductAggregate.class).exchange()
				.expectStatus().isEqualTo(expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return client.get().uri("/product-composite/" + productId).accept(MediaType.APPLICATION_JSON).exchange()
				.expectStatus().isEqualTo(expectedStatus).expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}

}