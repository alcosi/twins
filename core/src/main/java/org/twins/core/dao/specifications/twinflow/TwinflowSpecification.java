package org.twins.core.dao.specifications.twinflow;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twinflow.TwinflowEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

@Slf4j
public class TwinflowSpecification {

    public static Specification<TwinflowEntity> checkUuidIn(final String fieldName, final Collection<UUID> uuids, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(uuids)) return cb.conjunction();
            return not ? root.get(fieldName).in(uuids).not() : root.get(fieldName).in(uuids);
        };
    }

    public static Specification<TwinflowEntity> checkFieldLikeIn(final String fieldName, final Collection<String> search, final boolean or) {
        return (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search))
                for (String s : search) {
                    Predicate predicate = cb.like(cb.lower(root.get(fieldName)), s.toLowerCase());
                    predicates.add(predicate);
                }
            return getPredicate(cb, predicates, or);
        };
    }
}
