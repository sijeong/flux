package net.cfxp.microservice.core.recommendation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

import net.cfxp.microservice.core.recommendation.persistence.RecommendationEntity;

@SpringBootApplication
@ComponentScan("net.cfxp")
public class RecommendationServiceApplication {

	private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceApplication.class);

	public static void main(String[] args) {
		// SpringApplication.run(RecommendationServiceApplication.class, args);
		ConfigurableApplicationContext ctx = SpringApplication.run(RecommendationServiceApplication.class, args);

		String mongoDbHost = ctx.getEnvironment().getProperty("spring.data.mongodb.host");
		String mongoDbPort = ctx.getEnvironment().getProperty("spring.data.mongodb.port");

		LOG.info("Connected to MongoDb: " + mongoDbHost + ":" + mongoDbPort);
	}

	@Autowired
	MongoOperations mongoTemplate;

	@EventListener(ContextRefreshedEvent.class)
	public void initIndicesAfterStartup() {
		MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoTemplate
				.getConverter().getMappingContext();
		IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);

		IndexOperations indexOps = mongoTemplate.indexOps(RecommendationEntity.class);
		resolver.resolveIndexFor(RecommendationEntity.class).forEach(e -> indexOps.ensureIndex(e));
	}

}
