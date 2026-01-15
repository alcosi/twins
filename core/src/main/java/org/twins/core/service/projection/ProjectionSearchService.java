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
import org.twins.core.dao.projection.ProjectionEntity;
import org.twins.core.dao.projection.ProjectionRepository;
import org.twins.core.domain.search.ProjectionSearch;

import static org.twins.core.dao.specifications.CommonSpecification.checkTernary;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.projection.ProjectionSpecification.checkFieldProjectorIdIn;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class ProjectionSearchService {
    private final ProjectionRepository projectionRepository;

    public PaginationResult<ProjectionEntity> findProjections(ProjectionSearch search, SimplePagination pagination) throws ServiceException {
        Specification<ProjectionEntity> spec = createProjectionSearchSpecification(search);
        Page<ProjectionEntity> ret = projectionRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<ProjectionEntity> createProjectionSearchSpecification(ProjectionSearch search) {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, ProjectionEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, ProjectionEntity.Fields.id),
                checkUuidIn(search.getSrcTwinPointerIdList(), false, false, ProjectionEntity.Fields.srcTwinPointerId),
                checkUuidIn(search.getSrcTwinPointerIdExcludeList(), true, false, ProjectionEntity.Fields.srcTwinPointerId),
                checkUuidIn(search.getSrcTwinClassFieldIdList(), false, false, ProjectionEntity.Fields.srcTwinClassFieldId),
                checkUuidIn(search.getSrcTwinClassFieldIdExcludeList(), true, false, ProjectionEntity.Fields.srcTwinClassFieldId),
                checkUuidIn(search.getDstTwinClassIdList(), false, false, ProjectionEntity.Fields.dstTwinClassId),
                checkUuidIn(search.getDstTwinClassIdExcludeList(), true, false, ProjectionEntity.Fields.dstTwinClassId),
                checkUuidIn(search.getDstTwinClassFieldIdList(), false, false, ProjectionEntity.Fields.dstTwinClassFieldId),
                checkUuidIn(search.getDstTwinClassFieldIdExcludeList(), true, false, ProjectionEntity.Fields.dstTwinClassFieldId),
                checkUuidIn(search.getProjectionTypeIdList(), false, false, ProjectionEntity.Fields.projectionTypeId),
                checkUuidIn(search.getProjectionTypeIdExcludeList(), true, false, ProjectionEntity.Fields.projectionTypeId),
                checkTernary(search.getActive(), ProjectionEntity.Fields.active),
                checkFieldProjectorIdIn(search.getFieldProjectorIdList(), false),
                checkFieldProjectorIdIn(search.getFieldProjectorIdExcludeList(), true)
        );
    }
}
