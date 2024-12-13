package org.twins.core.dao.specifications.twin;

import jakarta.persistence.criteria.Join;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Slf4j
public class TwinFieldSimpleSpecification {

    private TwinFieldSimpleSpecification() {
    }

    public static Specification<TwinFieldSimpleEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            if (domainId == null)
                return cb.disjunction();
            Join<TwinFieldSimpleEntity, TwinClassFieldEntity> classFieldjoin = root.join(TwinFieldSimpleEntity.Fields.twinClassField);
            Join<TwinClassFieldEntity, TwinClassEntity> twinClassjoin = classFieldjoin.join(TwinClassFieldEntity.Fields.twinClass);
            return cb.equal(twinClassjoin.get(TwinClassEntity.Fields.domainId), domainId);
        };
    }


}

