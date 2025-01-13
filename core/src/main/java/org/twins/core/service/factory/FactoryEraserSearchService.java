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
import org.twins.core.dao.factory.*;
import org.twins.core.domain.search.FactoryEraserSearch;

import java.util.Set;
import java.util.stream.Collectors;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.factory.FactoryEraserSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryEraserSearchService {
    private final TwinFactoryEraserRepository twinFactoryEraserRepository;

    public PaginationResult<TwinFactoryEraserEntity> findFactoryEraser(FactoryEraserSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryEraserEntity> spec = createFactoryEraserSearchSpecification(search);
        Page<TwinFactoryEraserEntity> ret = twinFactoryEraserRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryEraserEntity> createFactoryEraserSearchSpecification(FactoryEraserSearch search) {
        return Specification.where(
                checkFieldLikeIn(TwinFactoryEraserEntity.Fields.description, search.getDescriptionLikeList(), false, true)
                        .and(checkFieldLikeIn(TwinFactoryEraserEntity.Fields.description, search.getDescriptionNotLikeList(), true, true))
                        .and(checkUuidIn(TwinFactoryEraserEntity.Fields.id, search.getIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryEraserEntity.Fields.id, search.getIdExcludeList(), true, false))
                        .and(checkUuidIn(TwinFactoryEraserEntity.Fields.twinFactoryId, search.getFactoryIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryEraserEntity.Fields.twinFactoryId, search.getFactoryIdExcludeList(), true, false))
                        .and(checkUuidIn(TwinFactoryEraserEntity.Fields.inputTwinClassId, search.getInputTwinClassIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryEraserEntity.Fields.inputTwinClassId, search.getInputTwinClassIdExcludeList(), true, false))
                        .and(checkUuidIn(TwinFactoryEraserEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdList(), false, false))
                        .and(checkUuidIn(TwinFactoryEraserEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdExcludeList(), true, false))
                        .and(checkFieldLikeIn(TwinFactoryEraserEntity.Fields.eraserAction, safeConvert(search.getEraseActionLikeList()), false, true))
                        .and(checkFieldLikeIn(TwinFactoryEraserEntity.Fields.eraserAction, safeConvert(search.getEraseActionNotLikeList()), true, true))
                        .and(checkTernary(TwinFactoryEraserEntity.Fields.active, search.getActive()))
        );
    }

    private Set<String> safeConvert(Set<TwinFactoryEraserEntity.Action> collection) {
        return collection.stream().map(Enum::name).collect(Collectors.toSet());
    }
}
