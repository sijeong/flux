package net.cfxp.microservices.core.recommendation;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

// import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;

import net.cfxp.api.core.recommendation.Recommendation;
import net.cfxp.microservices.core.recommendation.persistence.RecommendationRepository;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class RecommendationServiceApplicationTests extends MongoDbTestBase {

	@Autowired
	private WebTestClient client;

	@Autowired
	private RecommendationRepository repository;

	@BeforeEach
	void setupDb() {
		repository.deleteAll();
	}

	@Test
	void getRecommendationByProductId() {
		int productId = 1;
		postAndVerifyRecommendation(productId, 1, HttpStatus.OK);
		postAndVerifyRecommendation(productId, 2, HttpStatus.OK);
		postAndVerifyRecommendation(productId, 3, HttpStatus.OK);

		assertEquals(3, repository.findByProductId(productId).size());

		getAndVerifyRecommendationsByProductId(productId, HttpStatus.OK).jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[2].productId").isEqualTo(productId).jsonPath("$[2].recommendationId").isEqualTo(3);

	}

	// @Test
	// void contextLoads() {
	// }

	private WebTestClient.BodyContentSpec postAndVerifyRecommendation(int productId, int recommendationId,
			HttpStatus expectedStatus) {
		Recommendation recommendation = new Recommendation(productId, recommendationId, "Author " + recommendationId,
				recommendationId, "Content " + recommendationId, "SA");
		return client.post().uri("/recommendation").body(Mono.just(recommendation), Recommendation.class)
				.accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isEqualTo(expectedStatus).expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody();

	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(int productId,
			HttpStatus expectedStatus) {
		return getAndVerifyRecommendationsByProductId("?productId=" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(String productIdQuery,
			HttpStatus expectedStatus) {
		return client.get().uri("/recommendation" + productIdQuery).accept(MediaType.APPLICATION_JSON).exchange()
				.expectStatus().isEqualTo(expectedStatus).expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyRecommendationsByProductId(int productId,
			HttpStatus expectedStatus) {
		return client.delete().uri("/recommendation?productId=" + productId).accept(MediaType.APPLICATION_JSON)
				.exchange().expectStatus().isEqualTo(expectedStatus).expectBody();
	}
}
