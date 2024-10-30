package org.twins.core.dao.validator;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinValidatorSetRepository extends CrudRepository<TwinValidatorSetEntity, UUID> {
        TwinValidatorSetEntity findAllByIdAndDomainId(UUID id, UUID domainId);
        List<TwinValidatorSetEntity> findAllByIdInAndDomainId(Collection<UUID> id, UUID domainId);
}