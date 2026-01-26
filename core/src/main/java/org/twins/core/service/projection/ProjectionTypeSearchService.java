package org.twins.core.service.projection;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dao.projection.ProjectionTypeRepository;
import org.twins.core.domain.search.ProjectionTypeSearch;

import java.util.List;

import static org.twins.core.dao.specifications.CommonSpecification.checkFieldLikeIn;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;

// Log calls that took more than 2 seconds
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class ProjectionTypeSearchService {
    private final ProjectionTypeRepository projectionTypeRepository;

    public List<ProjectionTypeEntity> findProjectionTypes(ProjectionTypeSearch search) throws ServiceException {
        Specification<ProjectionTypeEntity> spec = createSpecification(search);
        return projectionTypeRepository.findAll(spec);
    }

    public PaginationResult<ProjectionTypeEntity> findProjectionTypes(ProjectionTypeSearch search, SimplePagination pagination) throws ServiceException {
        Specification<ProjectionTypeEntity> spec = createSpecification(search);
        Page<ProjectionTypeEntity> ret = projectionTypeRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<ProjectionTypeEntity> createSpecification(ProjectionTypeSearch search) throws ServiceException {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, ProjectionTypeEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, ProjectionTypeEntity.Fields.id),
                checkFieldLikeIn(search.getKeyLikeList(), false, true, ProjectionTypeEntity.Fields.key),
                checkFieldLikeIn(search.getKeyNotLikeList(), true, true, ProjectionTypeEntity.Fields.key),
                checkFieldLikeIn(search.getNameLikeList(), false, true, ProjectionTypeEntity.Fields.name),
                checkFieldLikeIn(search.getNameNotLikeList(), true, true, ProjectionTypeEntity.Fields.name),
                checkUuidIn(search.getProjectionTypeGroupIdList(), false, false, ProjectionTypeEntity.Fields.projectionTypeGroupId),
                checkUuidIn(search.getProjectionTypeGroupIdExcludeList(), true, false, ProjectionTypeEntity.Fields.projectionTypeGroupId),
                checkUuidIn(search.getMembershipTwinClassIdList(), false, false, ProjectionTypeEntity.Fields.membershipTwinClassId),
                checkUuidIn(search.getMembershipTwinClassIdExcludeList(), true, false, ProjectionTypeEntity.Fields.membershipTwinClassId)
        );
    }
}
