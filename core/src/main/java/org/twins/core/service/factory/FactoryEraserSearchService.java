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

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.factory.FactoryEraserSpecification.checkFieldLikeIn;
import static org.twins.core.dao.specifications.factory.FactoryEraserSpecification.checkTernary;


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

    private Specification<TwinFactoryEraserEntity> createFactoryEraserSearchSpecification(FactoryEraserSearch search) {
        return Specification.allOf(
                checkFieldLikeIn(TwinFactoryEraserEntity.Fields.description, search.getDescriptionLikeList(), false, true),
                checkFieldLikeIn(TwinFactoryEraserEntity.Fields.description, search.getDescriptionNotLikeList(), true, true),
                checkUuidIn(TwinFactoryEraserEntity.Fields.id, search.getIdList(), false, false),
                checkUuidIn(TwinFactoryEraserEntity.Fields.id, search.getIdExcludeList(), true, false),
                checkUuidIn(TwinFactoryEraserEntity.Fields.twinFactoryId, search.getFactoryIdList(), false, false),
                checkUuidIn(TwinFactoryEraserEntity.Fields.twinFactoryId, search.getFactoryIdExcludeList(), true, false),
                checkUuidIn(TwinFactoryEraserEntity.Fields.inputTwinClassId, search.getInputTwinClassIdList(), false, false),
                checkUuidIn(TwinFactoryEraserEntity.Fields.inputTwinClassId, search.getInputTwinClassIdExcludeList(), true, false),
                checkUuidIn(TwinFactoryEraserEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdList(), false, false),
                checkUuidIn(TwinFactoryEraserEntity.Fields.twinFactoryConditionSetId, search.getFactoryConditionSetIdExcludeList(), true, false),
                checkFieldLikeIn(TwinFactoryEraserEntity.Fields.eraserAction, safeConvert(search.getEraseActionLikeList()), false, true),
                checkFieldLikeIn(TwinFactoryEraserEntity.Fields.eraserAction, safeConvert(search.getEraseActionNotLikeList()), true, true),
                checkTernary(TwinFactoryEraserEntity.Fields.active, search.getActive()));
    }

    private Set<String> safeConvert(Set<TwinFactoryEraserEntity.Action> collection) {
        return collection == null ? Collections.emptySet() : collection.stream().map(Enum::name).collect(Collectors.toSet());
    }
}
