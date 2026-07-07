package org.twins.core.service.twinclass;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleMapEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleRepository;
import org.twins.core.domain.search.TwinClassFieldRuleSearch;

import java.util.Set;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkIntegerIn;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassFieldRuleSearchService {

    private final TwinClassFieldRuleRepository twinClassFieldRuleRepository;

    public PaginationResult<TwinClassFieldRuleEntity> findTwinClassFieldRules(TwinClassFieldRuleSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinClassFieldRuleEntity> spec = createTwinClassFieldRuleSearchSpecification(search);
        Page<TwinClassFieldRuleEntity> ret = twinClassFieldRuleRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinClassFieldRuleEntity> createTwinClassFieldRuleSearchSpecification(TwinClassFieldRuleSearch search) throws ServiceException {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinClassFieldRuleEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinClassFieldRuleEntity.Fields.id),
                checkTwinClassFieldIdIn(search.getTwinClassFieldIdList(), false),
                checkTwinClassFieldIdIn(search.getTwinClassFieldIdExcludeList(), true),
                checkIntegerIn(search.getFieldOverwriterFeaturerIdList(), false, TwinClassFieldRuleEntity.Fields.fieldOverwriterFeaturerId),
                checkIntegerIn(search.getFieldOverwriterFeaturerIdExcludeList(), true, TwinClassFieldRuleEntity.Fields.fieldOverwriterFeaturerId)
        );
    }

    private Specification<TwinClassFieldRuleEntity> checkTwinClassFieldIdIn(Set<UUID> twinClassFieldIds, boolean not) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(twinClassFieldIds))
                return cb.conjunction();
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<TwinClassFieldRuleMapEntity> subRoot = subquery.from(TwinClassFieldRuleMapEntity.class);
            subquery.select(subRoot.get(TwinClassFieldRuleMapEntity.Fields.twinClassFieldRuleId));
            subquery.where(subRoot.get(TwinClassFieldRuleMapEntity.Fields.twinClassFieldId).in(twinClassFieldIds));
            return not ? cb.not(root.get(TwinClassFieldRuleEntity.Fields.id).in(subquery)) : root.get(TwinClassFieldRuleEntity.Fields.id).in(subquery);
        };
    }
}
