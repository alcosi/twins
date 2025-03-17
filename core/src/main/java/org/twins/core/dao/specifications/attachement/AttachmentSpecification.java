package org.twins.core.dao.specifications.attachement;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.Collection;
import java.util.List;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class AttachmentSpecification extends CommonSpecification<TwinAttachmentEntity> {

    //TODO test it.
    public static <T> Specification<T> checkMapFieldLikeIn(
            final Collection<String> search,
            final boolean not, final boolean or, final boolean includeNullValues,
            final String mapFieldName, final String valueFieldName) {

        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search)) {
                return cb.conjunction();
            }
            Join<T, ?> mapJoin = root.join(mapFieldName, includeNullValues ? JoinType.LEFT : JoinType.INNER);
            List<Predicate> predicates = search.stream().map(value -> {
                Predicate predicate = cb.like(cb.lower(mapJoin.get(valueFieldName)), value.toLowerCase());
                if (not) predicate = cb.not(predicate);
                return predicate;
            }).toList();

            return getPredicate(cb, predicates, or);
        };
    }

}
