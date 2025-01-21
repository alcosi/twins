package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dao.factory.TwinFactoryBranchRepository;
import org.twins.core.domain.search.FactoryBranchSearch;
import org.twins.core.service.auth.AuthService;

import java.util.Optional;
import java.util.UUID;

import static org.twins.core.dao.specifications.CommonSpecification.checkFieldUuid;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.factory.FactoryBranchSpecification.checkFieldLikeIn;
import static org.twins.core.dao.specifications.factory.FactoryBranchSpecification.checkTernary;


@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryBranchSearchService {
    private final TwinFactoryBranchRepository twinFactoryBranchRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public TwinFactoryBranchEntity findFactoryBrancherById(UUID id) throws ServiceException {
        Optional<TwinFactoryBranchEntity> entity = twinFactoryBranchRepository.findBy(
                Specification.allOf(
                        checkFieldUuid(id, TwinFactoryBranchEntity.Fields.id)
                ), FluentQuery.FetchableFluentQuery::one
        );
        return entity.orElse(null);
    }
    public PaginationResult<TwinFactoryBranchEntity> findFactoryBranches(FactoryBranchSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryBranchEntity> spec = createFactoryBranchSearchSpecification(search);
        Page<TwinFactoryBranchEntity> ret = twinFactoryBranchRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryBranchEntity> createFactoryBranchSearchSpecification(FactoryBranchSearch search) {
        return Specification.allOf(
                checkFieldLikeIn(TwinFactoryBranchEntity.Fields.description, search.getDescriptionLikeList(), false, true),
                checkFieldLikeIn(TwinFactoryBranchEntity.Fields.description, search.getDescriptionNotLikeList(), true, true),
                checkUuidIn(TwinFactoryBranchEntity.Fields.id, search.getIdList(), false, false),
                checkUuidIn(TwinFactoryBranchEntity.Fields.id, search.getIdExcludeList(), true, false),
                checkUuidIn(TwinFactoryBranchEntity.Fields.twinFactoryId, search.getFactoryIdList(), false, false),
                checkUuidIn(TwinFactoryBranchEntity.Fields.twinFactoryId, search.getFactoryIdExcludeList(), true, false),
                checkUuidIn(TwinFactoryBranchEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdList(), false, false),
                checkUuidIn(TwinFactoryBranchEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdExcludeList(), true, true),
                checkUuidIn(TwinFactoryBranchEntity.Fields.nextTwinFactoryId, search.getNextFactoryIdList(), false, false),
                checkUuidIn(TwinFactoryBranchEntity.Fields.nextTwinFactoryId, search.getNextFactoryIdExcludeList(), true, false),
                checkTernary(TwinFactoryBranchEntity.Fields.active, search.getActive()));
    }
}
