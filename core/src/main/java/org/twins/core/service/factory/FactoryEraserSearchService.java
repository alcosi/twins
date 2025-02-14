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
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.dao.factory.TwinFactoryEraserRepository;
import org.twins.core.domain.search.FactoryEraserSearch;
import org.twins.core.service.auth.AuthService;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.twins.core.dao.specifications.factory.FactoryEraserSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryEraserSearchService {
    private final TwinFactoryEraserRepository twinFactoryEraserRepository;
    private final AuthService authService;

    public PaginationResult<TwinFactoryEraserEntity> findFactoryEraser(FactoryEraserSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryEraserEntity> spec = createFactoryEraserSearchSpecification(search);
        Page<TwinFactoryEraserEntity> ret = twinFactoryEraserRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryEraserEntity> createFactoryEraserSearchSpecification(FactoryEraserSearch search) throws ServiceException {
        return Specification.allOf(
                checkDomainId(authService.getApiUser().getDomainId()),
                checkFieldLikeIn(search.getDescriptionLikeList(), false, true, TwinFactoryEraserEntity.Fields.description),
                checkFieldLikeIn(search.getDescriptionNotLikeList(), true, true, TwinFactoryEraserEntity.Fields.description),
                checkUuidIn(search.getIdList(), false, false, TwinFactoryEraserEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryEraserEntity.Fields.id),
                checkUuidIn(search.getFactoryIdList(), false, false, TwinFactoryEraserEntity.Fields.twinFactoryId),
                checkUuidIn(search.getFactoryIdExcludeList(), true, false, TwinFactoryEraserEntity.Fields.twinFactoryId),
                checkUuidIn(search.getInputTwinClassIdList(), false, false, TwinFactoryEraserEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getInputTwinClassIdExcludeList(), true, false, TwinFactoryEraserEntity.Fields.inputTwinClassId),
                checkUuidIn(search.getFactoryConditionSetIdList(), false, false, TwinFactoryEraserEntity.Fields.twinFactoryConditionSetId),
                checkUuidIn(search.getFactoryConditionSetIdExcludeList(), true, false, TwinFactoryEraserEntity.Fields.twinFactoryConditionSetId),
                checkFieldLikeIn(safeConvert(search.getEraseActionLikeList()), false, true, TwinFactoryEraserEntity.Fields.eraserAction),
                checkFieldLikeIn(safeConvert(search.getEraseActionNotLikeList()), true, true, TwinFactoryEraserEntity.Fields.eraserAction),
                checkTernary(search.getActive(), TwinFactoryEraserEntity.Fields.active));
    }

    private Set<String> safeConvert(Set<TwinFactoryEraserEntity.Action> collection) {
        return collection == null ? Collections.emptySet() : collection.stream().map(Enum::name).collect(Collectors.toSet());
    }
}
