package net.cfxp.microservices.core.product.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import net.cfxp.api.core.product.Product;
import net.cfxp.microservices.core.product.persistence.ProductEntity;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mappings({ @Mapping(target = "serviceAddress", ignore = true) })
    Product entityToApi(ProductEntity entity);

    @Mappings({ @Mapping(target = "id", ignore = true), @Mapping(target = "version", ignore = true) })
    ProductEntity apiToEntity(Product api);
}
