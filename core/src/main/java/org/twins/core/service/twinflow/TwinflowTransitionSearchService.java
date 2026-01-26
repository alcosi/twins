package org.twins.core.service.twinflow;

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
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionRepository;
import org.twins.core.domain.search.TransitionSearch;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Locale;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.twinflow.TransitionSpecification.checkAliasLikeIn;
import static org.twins.core.dao.specifications.twinflow.TransitionSpecification.checkTransitionTypeLikeIn;


@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
public class TwinflowTransitionSearchService {
    private final TwinflowTransitionRepository twinflowTransitionRepository;
    private final AuthService authService;
    private final TwinClassService twinClassService;

    private Specification<TwinflowTransitionEntity> createTwinflowTransitionEntitySearchSpecification(TransitionSearch search) throws ServiceException {
        Locale locale = authService.getApiUser().getLocale();
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinflowTransitionEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinflowTransitionEntity.Fields.id),
                joinAndSearchByI18NField(TwinflowTransitionEntity.Fields.nameI18n, search.getNameLikeList(), locale, true, false),
                joinAndSearchByI18NField(TwinflowTransitionEntity.Fields.nameI18n, search.getNameNotLikeList(), locale, true, true),
                joinAndSearchByI18NField(TwinflowTransitionEntity.Fields.descriptionI18n, search.getDescriptionLikeList(), locale, true, false),
                joinAndSearchByI18NField(TwinflowTransitionEntity.Fields.descriptionI18n, search.getDescriptionNotLikeList(), locale, true, true),
                checkUuidIn(twinClassService.loadExtendsHierarchyClasses(search.getTwinClassIdMap()), false, false, TwinflowTransitionEntity.Fields.twinflow, TwinflowEntity.Fields.twinClassId),
                checkUuidIn(twinClassService.loadExtendsHierarchyClasses(search.getTwinClassIdExcludeMap()), true, false, TwinflowTransitionEntity.Fields.twinflow, TwinflowEntity.Fields.twinClassId),
                checkUuidIn(search.getTwinflowIdList(), false, false, TwinflowTransitionEntity.Fields.twinflowId),
                checkUuidIn(search.getTwinflowIdExcludeList(), true, false, TwinflowTransitionEntity.Fields.twinflowId),
                checkUuidIn(search.getSrcStatusIdList(), false, false, TwinflowTransitionEntity.Fields.srcTwinStatusId),
                checkUuidIn(search.getSrcStatusIdExcludeList(), true, true, TwinflowTransitionEntity.Fields.srcTwinStatusId),
                checkUuidIn(search.getDstStatusIdList(), false, false, TwinflowTransitionEntity.Fields.dstTwinStatusId),
                checkUuidIn(search.getDstStatusIdExcludeList(), true, false, TwinflowTransitionEntity.Fields.dstTwinStatusId),
                checkAliasLikeIn(search.getAliasLikeList(), true),
                checkUuidIn(search.getPermissionIdList(), false, false, TwinflowTransitionEntity.Fields.permissionId),
                checkUuidIn(search.getPermissionIdExcludeList(), true, true, TwinflowTransitionEntity.Fields.permissionId),
                checkUuidIn(search.getInbuiltTwinFactoryIdList(), false, false, TwinflowTransitionEntity.Fields.inbuiltTwinFactoryId),
                checkUuidIn(search.getInbuiltTwinFactoryIdExcludeList(), true, true, TwinflowTransitionEntity.Fields.inbuiltTwinFactoryId),
                checkUuidIn(search.getDraftingTwinFactoryIdList(), false, false, TwinflowTransitionEntity.Fields.draftingTwinFactoryId),
                checkUuidIn(search.getDraftingTwinFactoryIdExcludeList(), true, true, TwinflowTransitionEntity.Fields.draftingTwinFactoryId),
                checkTransitionTypeLikeIn(search.getTwinflowTransitionTypeList(), false),
                checkTransitionTypeLikeIn(search.getTwinflowTransitionTypeExcludeList(), true)
        );
    }

    public PaginationResult<TwinflowTransitionEntity> findTransitions(TransitionSearch transitionSearch, SimplePagination pagination) throws ServiceException {
        Specification<TwinflowTransitionEntity> spec = createTwinflowTransitionEntitySearchSpecification(transitionSearch);
        Page<TwinflowTransitionEntity> ret = twinflowTransitionRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }
}
