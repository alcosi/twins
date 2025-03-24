package org.twins.core.dao.specifications.twinflow;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twinflow.TwinflowTransitionAliasEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;

import java.util.ArrayList;
import java.util.Collection;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

@Slf4j
public class TransitionSpecification extends CommonSpecification<TwinflowTransitionEntity> {

    public static Specification<TwinflowTransitionEntity> checkAliasLikeIn(final Collection<String> search, final boolean or) {
        return (root, query, cb) -> {
            Join<TwinflowTransitionEntity, TwinflowTransitionAliasEntity> twinflowAliasJoin = root.join(TwinflowTransitionEntity.Fields.twinflowTransitionAlias, JoinType.INNER);
            ArrayList<Predicate> predicates = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(search))
                for (String s : search) {
                    Predicate predicate = cb.like(cb.lower(twinflowAliasJoin.get(TwinflowTransitionAliasEntity.Fields.alias)), "%" + s.toLowerCase() + "%");
                    predicates.add(predicate);
                }
            return getPredicate(cb, predicates, or);
        };
    }
}
