package net.cfxp.microservices.core.review;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import net.cfxp.api.core.review.Review;
import net.cfxp.microservices.core.review.persistence.ReviewRepository;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReviewServiceApplicationTests extends MySqlTestBase {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ReviewRepository repository;

	@BeforeEach
	void setupDb() {
		repository.deleteAll();
	}

	@Test
	void getReviewsByProductId() {
		int productId = 1;

		assertEquals(0, repository.findByProductId(productId).size());

		postAndVerifyReview(productId, 1, HttpStatus.OK);
		postAndVerifyReview(productId, 2, HttpStatus.OK);
		postAndVerifyReview(productId, 3, HttpStatus.OK);

		assertEquals(3, repository.findByProductId(productId).size());

		getAndVerifyReviewsByProductId(productId, HttpStatus.OK).jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[2].productId").isEqualTo(productId).jsonPath("$[2].reviewId").isEqualTo(3);
	}
	// @Test
	// void contextLoads() {
	// }

	private WebTestClient.BodyContentSpec postAndVerifyReview(int productId, int reviewId, HttpStatus expectedStatus) {
		Review review = new Review(productId, reviewId, "Author " + reviewId, "Subject " + reviewId,
				"Content " + reviewId, "SA");
		return client.post().uri("/review").body(Mono.just(review), Review.class).accept(MediaType.APPLICATION_JSON)
				.exchange().expectStatus().isEqualTo(expectedStatus).expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody();
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
		return getAndVerifyReviewByProductId("?productId=" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewByProductId(String productIdQuery,
			HttpStatus exHttpStatus) {
		return client.get().uri("/review" + productIdQuery).accept(MediaType.APPLICATION_JSON).exchange().expectStatus()
				.isEqualTo(exHttpStatus).expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody();
	}

}
