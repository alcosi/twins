package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TwinPointerValidatorRuleRepository extends JpaRepository<TwinPointerValidatorRuleEntity, UUID> {
}
