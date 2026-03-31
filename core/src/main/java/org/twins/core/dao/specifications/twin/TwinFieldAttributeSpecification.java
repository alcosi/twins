package org.twins.core.dao.specifications.twin;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twin.TwinFieldAttributeEntity;

import java.util.*;

public class TwinFieldAttributeSpecification {
    public static Specification<TwinFieldAttributeEntity> byTwinIdAndFieldIds(
            Map<UUID, Set<UUID>> twinIdToFieldIds
    ) {
        return (root, query, cb) -> {
            List<Predicate> orPredicates = new ArrayList<>();
            for (Map.Entry<UUID, Set<UUID>> entry : twinIdToFieldIds.entrySet()) {
                UUID twinId = entry.getKey();
                Set<UUID> fieldIds = entry.getValue();
                if (fieldIds == null || fieldIds.isEmpty()) {
                    continue;
                }
                Predicate twinPredicate = cb.equal(
                        root.get(TwinFieldAttributeEntity.Fields.twinId),
                        twinId
                );
                Predicate fieldPredicate = root.get(TwinFieldAttributeEntity.Fields.twinClassFieldId)
                        .in(fieldIds);
                orPredicates.add(
                        cb.and(twinPredicate, fieldPredicate)
                );
            }

            if (orPredicates.isEmpty()) {
                return cb.disjunction();
            }

            return cb.or(orPredicates.toArray(new Predicate[0]));
        };
    }
}
