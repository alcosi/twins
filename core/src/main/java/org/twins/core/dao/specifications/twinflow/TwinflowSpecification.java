package org.twins.core.dao.specifications.twinflow;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.specifications.CommonSpecification;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaMapEntity;

import java.util.Collection;
import java.util.UUID;

@Slf4j
public class TwinflowSpecification extends CommonSpecification<TwinflowEntity> {

    public static Specification<TwinflowEntity> checkSchemas(final String fieldName, final Collection<UUID> twinflowSchemaIds, final boolean not, boolean ifNotIsTrueIncludeNullValues) {
        return (root, query, cb) -> {
            if (twinflowSchemaIds == null || twinflowSchemaIds.isEmpty()) return cb.conjunction();
            Join<TwinflowEntity, TwinflowSchemaMapEntity> schemaMapping = root.join(fieldName, ifNotIsTrueIncludeNullValues ? JoinType.LEFT : JoinType.INNER);
            Predicate predicate = not
                    ? cb.not(schemaMapping.get(TwinflowSchemaMapEntity.Fields.twinflowSchemaId).in(twinflowSchemaIds))
                    : schemaMapping.get(TwinflowSchemaMapEntity.Fields.twinflowSchemaId).in(twinflowSchemaIds);

            if (ifNotIsTrueIncludeNullValues) {
                predicate = cb.or(predicate, cb.isNull(schemaMapping.get(TwinflowSchemaMapEntity.Fields.twinflowSchemaId)));
            }

            return predicate;
        };
    }
}
