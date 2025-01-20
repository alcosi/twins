package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryConditionSetRepository;
import org.twins.core.domain.search.FactoryConditionSetSearch;
import org.twins.core.service.auth.AuthService;

import static org.twins.core.dao.specifications.CommonSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryConditionSetSearchService {
    private final TwinFactoryConditionSetRepository twinFactoryConditionSetRepository;
    private final AuthService authService;

    public PaginationResult<TwinFactoryConditionSetEntity> findFactoryConditionSets(FactoryConditionSetSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryConditionSetEntity> spec = createFactoryConditionSetSearchSpecification(search);
        Page<TwinFactoryConditionSetEntity> ret = twinFactoryConditionSetRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryConditionSetEntity> createFactoryConditionSetSearchSpecification(FactoryConditionSetSearch search) throws ServiceException {
        return Specification.allOf(
                checkFieldUuid(authService.getApiUser().getDomainId(), TwinFactoryConditionSetEntity.Fields.domainId),
                checkFieldLikeIn(TwinFactoryConditionSetEntity.Fields.name, search.getNameLikeList(), false, true),
                checkFieldLikeIn(TwinFactoryConditionSetEntity.Fields.name, search.getNameNotLikeList(), true, true),
                checkFieldLikeIn(TwinFactoryConditionSetEntity.Fields.description, search.getDescriptionLikeList(), false, true),
                checkFieldLikeIn(TwinFactoryConditionSetEntity.Fields.description, search.getDescriptionNotLikeList(), true, true),
                checkUuidIn(TwinFactoryConditionSetEntity.Fields.id, search.getIdList(), false, false),
                checkUuidIn(TwinFactoryConditionSetEntity.Fields.id, search.getIdExcludeList(), true, false));
    }
}
