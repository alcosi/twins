package org.twins.core.dao.template.generator;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TemplateGeneratorRepository extends CrudRepository<TemplateGeneratorEntity, UUID>, JpaSpecificationExecutor<TemplateGeneratorEntity> {
    <T> T findById(UUID id, Class<T> type);
}
