package net.cfxp.microservice.core.recommendation.services;

import java.util.ArrayList;
import java.util.List;

import javax.print.ServiceUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.dao.DuplicateKeyException;

import net.cfxp.api.core.recommendation.Recommendation;
import net.cfxp.api.core.recommendation.RecommendationService;
import net.cfxp.api.exceptions.InvalidInputException;
import net.cfxp.api.util.http.ServiceUtil;
import net.cfxp.microservice.core.recommendation.persistence.RecommendationEntity;
import net.cfxp.microservice.core.recommendation.persistence.RecommendationRepository;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;
    private final ServiceUtil serviceUtil;

    public RecommendationServiceImpl(RecommendationRepository repository, RecommendationMapper mapper,
            ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        try {
            RecommendationEntity entity = mapper.apiToEntity(body);
            RecommendationEntity newEntity = repository.save(entity);

            LOG.debug("createREcommendation: created a recommendation entity: {}/{}", body.getProductId(),
                    body.getRecommendationId());
            return mapper.entityToApi(newEntity);
        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId()
                    + ", Recommendation Id: " + body.getRecommendationId());
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        // if (productId == 113) {
        // LOG.debug("No recommendation found for productId: {}", productId);
        // return new ArrayList<>();
        // }
        List<RecommendationEntity> entityList = repository.findByProductId(productId);
        List<Recommendation> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        // list.add(new Recommendation(productId, 1, "Author 1", 1, "Content 1",
        // serviceUtil.getServiceAddress()));
        // list.add(new Recommendation(productId, 2, "Author 2", 2, "Content 2",
        // serviceUtil.getServiceAddress()));
        // list.add(new Recommendation(productId, 3, "Author 3", 3, "Content 3",
        // serviceUtil.getServiceAddress()));

        LOG.debug("/recommendation response size: {}", list.size());

        return list;
    }

    @Override
    public void deleteRecommendation(int productId) {
        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}",
                productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
