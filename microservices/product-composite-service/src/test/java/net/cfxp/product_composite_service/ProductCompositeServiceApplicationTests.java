package net.cfxp.product_composite_service;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import net.cfxp.api.core.product.Product;
import net.cfxp.api.core.recommendation.Recommendation;
import net.cfxp.api.core.review.Review;
import net.cfxp.product_composite_service.services.ProductCompositeIntegration;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ProductCompositeServiceApplicationTests {

	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 2;
	private static final int PRODUCT_ID_INVALID = 3;

	@Autowired
	private WebTestClient client;
	@MockBean
	private ProductCompositeIntegration compositeIntegration;

	// @BeforeEach
	// void setUp() {
	// 	when(compositeIntegration.getProduct(PRODUCT_ID_OK))
	// 			.thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-address"));
	// 	when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
	// 			.thenReturn(
	// 					singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address")));
	// 	when(compositeIntegration.getReviews(PRODUCT_ID_OK))
	// 			.thenReturn(singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subjet", "content", "mock address")));
	// }

	// @Test
	// void contextLoads() {
	// }

	// @Test
	// void getProductId() {
	// 	client.get()
	// 			.uri("/product-composite/" + PRODUCT_ID_OK)
	// 			.accept(MediaType.APPLICATION_JSON)
	// 			.exchange()
	// 			.expectStatus().isOk()
	// 			.expectHeader().contentType(MediaType.APPLICATION_JSON)
	// 			.expectBody()
	// 			.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
	// 			.jsonPath("$.recommendations.length()").isEqualTo(1)
	// 			.jsonPath("$.reviews.length()").isEqualTo(1);
	// }
}
