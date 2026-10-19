package org.twins.core.dao.domain;

import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface DomainBusinessAccountRepository extends CrudRepository<DomainBusinessAccountEntity, UUID>, JpaSpecificationExecutor<DomainBusinessAccountEntity> {
    @Query(value = "select db, db.domainSpecOnly, db.businessAccountSpecOnly from DomainBusinessAccountEntity db where db.domainId = :domainId and db.businessAccountId = :businessAccountId")
    List<Object[]> _findByDomainIdAndBusinessAccountId(UUID domainId, UUID businessAccountId);

    default DomainBusinessAccountEntity findByDomainIdAndBusinessAccountId(UUID domainId, UUID businessAccountId) {
        var results = _findByDomainIdAndBusinessAccountId(domainId, businessAccountId);
        if (CollectionUtils.isEmpty(results))
            return null;
        var row = results.getFirst();
        var ret = (DomainBusinessAccountEntity) row[0];
        ret
                .setDomain((DomainEntity) row[1])
                .setBusinessAccount((BusinessAccountEntity) row[2]);
        return ret;
    }
}
