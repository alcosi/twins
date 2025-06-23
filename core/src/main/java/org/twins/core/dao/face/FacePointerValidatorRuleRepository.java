package org.twins.core.dao.face;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FacePointerValidatorRuleRepository extends JpaRepository<FacePointerValidatorRuleEntity, UUID> {
}
