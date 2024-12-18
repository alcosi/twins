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

import static org.twins.core.dao.specifications.factory.FactoryConditionSetSpecification.checkFieldLikeIn;
import static org.twins.core.dao.specifications.factory.FactoryConditionSetSpecification.checkUuidIn;


@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryConditionSetSearchService {
    private final TwinFactoryConditionSetRepository twinFactoryConditionSetRepository;

    public PaginationResult<TwinFactoryConditionSetEntity> findFactoryConditionSets(FactoryConditionSetSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryConditionSetEntity> spec = createFactoryConditionSetSearchSpecification(search);
        Page<TwinFactoryConditionSetEntity> ret = twinFactoryConditionSetRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryConditionSetEntity> createFactoryConditionSetSearchSpecification(FactoryConditionSetSearch search) {
        return Specification.where(
                checkFieldLikeIn(TwinFactoryConditionSetEntity.Fields.name, search.getNameLikeList(), false, true)
                        .and(checkFieldLikeIn(TwinFactoryConditionSetEntity.Fields.name, search.getNameNotLikeList(), true, true))
                        .and(checkFieldLikeIn(TwinFactoryConditionSetEntity.Fields.description, search.getDescriptionLikeList(), false, true))
                        .and(checkFieldLikeIn(TwinFactoryConditionSetEntity.Fields.description, search.getDescriptionNotLikeList(), true, true))
                        .and(checkUuidIn(TwinFactoryConditionSetEntity.Fields.id, search.getIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryConditionSetEntity.Fields.id, search.getIdExcludeList(), true, false))
        );
    }
}
