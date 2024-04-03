package org.twins.core.dao.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface DomainLocaleRepository extends CrudRepository<DomainLocaleEntity, UUID> {
}
