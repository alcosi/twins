package org.twins.core.service.projection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.projection.ProjectionExclusionEntity;
import org.twins.core.dao.projection.ProjectionExclusionRepository;
import org.twins.core.dto.rest.projection.ProjectionExclusionSearch;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectionExclusionSearchService {
    private final ProjectionExclusionRepository projectionExclusionRepository;

    public PaginationResult<ProjectionExclusionEntity> findProjectionExclusions(ProjectionExclusionSearch search, SimplePagination pagination) throws ServiceException {
        Specification<ProjectionExclusionEntity> spec = createProjectionExclusionSearchSpecification(search);
        Page<ProjectionExclusionEntity> ret = projectionExclusionRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<ProjectionExclusionEntity> createProjectionExclusionSearchSpecification(ProjectionExclusionSearch search) {
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false,false, ProjectionExclusionEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true,false, ProjectionExclusionEntity.Fields.id),
                checkUuidIn(search.getTwinIdList(), false,false, ProjectionExclusionEntity.Fields.twinId),
                checkUuidIn(search.getTwinIdExcludeList(), true,false, ProjectionExclusionEntity.Fields.twinId),
                checkUuidIn(search.getTwinClassFieldIdList(), false,false, ProjectionExclusionEntity.Fields.twinClassFieldId),
                checkUuidIn(search.getTwinClassFieldIdExcludeList(), true, false, ProjectionExclusionEntity.Fields.twinClassFieldId)
        );
    }
}
