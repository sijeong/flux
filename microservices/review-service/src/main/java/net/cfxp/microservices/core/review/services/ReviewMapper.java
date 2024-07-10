package net.cfxp.microservices.core.review.services;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import net.cfxp.api.core.review.Review;
import net.cfxp.microservices.core.review.persistence.ReviewEntity;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mappings({ @Mapping(target = "serviceAddress", ignore = true) })
    Review entityToApi(ReviewEntity api);

    @Mappings({ @Mapping(target = "id", ignore = true), @Mapping(target = "version", ignore = true) })
    ReviewEntity apiToEntity(Review api);

    List<Review> entityListToApiList(List<ReviewEntity> entity);

    List<ReviewEntity> apiListToEntityList(List<Review> api);
}
