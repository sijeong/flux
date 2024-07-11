package net.cfxp.microservices.core.product;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import net.cfxp.api.core.product.Product;
import net.cfxp.microservices.core.product.persistence.ProductRepository;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests extends MongoDbTestBase {

	@Autowired
	private WebTestClient client;
	@Autowired
	private ProductRepository repository;

	@BeforeEach
	void setupDb() {
		repository.deleteAll();
	}

	@Test
	void getProductById() {
		int productId = 1;

		postAndVerifyProduct(productId, HttpStatus.OK);
		assertTrue(repository.findByProductId(productId).isPresent());
		getAndVerifyProduct(productId, HttpStatus.OK).jsonPath("$.productId").isEqualTo(productId);
	}
	// @Test
	// void contextLoads() {
	// }

	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return getAndVerifyProduct("/" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
		return client.get().uri("/product" + productIdPath).accept(MediaType.APPLICATION_JSON).exchange().expectStatus()
				.isEqualTo(expectedStatus).expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		Product product = new Product(productId, "Name " + productId, productId, "SA");
		return client.post().uri("/product").body(Mono.just(product), Product.class).accept(MediaType.APPLICATION_JSON)
				.exchange().expectStatus().isEqualTo(expectedStatus).expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return client.delete().uri("/product/" + productId).accept(MediaType.APPLICATION_JSON).exchange().expectStatus()
				.isEqualTo(expectedStatus).expectBody();
	}
}
