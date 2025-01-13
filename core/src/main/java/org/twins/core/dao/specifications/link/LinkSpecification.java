package org.twins.core.dao.specifications.link;

import jakarta.persistence.criteria.Predicate;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class LinkSpecification extends CommonSpecification<TwinFactoryEntity> {

    public static Specification<LinkEntity> checkFieldLikeIn(String field, Collection<String> search, boolean not, boolean or) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search))
                return cb.conjunction();

            List<Predicate> predicates = new ArrayList<>();
            for (String value : search) {
                Predicate predicate = cb.like(cb.lower(root.get(field)), value.toLowerCase());
                if (not) predicate = cb.not(predicate);
                predicates.add(predicate);
            }
            return getPredicate(cb, predicates, or);
        };
    }

    public static Specification<LinkEntity> checkSrcOrDstTwinClassIdIn(Collection<UUID> search, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(search))
                return cb.conjunction();

            Predicate srcTwinClassIdPredicate = root.get(LinkEntity.Fields.srcTwinClassId).in(search);
            Predicate dstTwinClassIdPredicate = root.get(LinkEntity.Fields.dstTwinClassId).in(search);

            Predicate predicate = cb.or(srcTwinClassIdPredicate, dstTwinClassIdPredicate);
            if (not) predicate = cb.not(predicate);
            return predicate;
        };
    }

    public static Specification<LinkEntity> checkDomainId(UUID domainId) {
        return (root, query, cb) -> {
            if (domainId == null)
                return cb.disjunction();
            return cb.equal(root.get(LinkEntity.Fields.domainId), domainId);
        };
    }
}
