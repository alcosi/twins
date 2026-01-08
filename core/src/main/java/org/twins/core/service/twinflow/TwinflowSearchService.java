package org.twins.core.service.twinflow;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowRepository;
import org.twins.core.domain.search.TwinflowSearch;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Locale;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.twinflow.TwinflowSpecification.checkSchemas;
import static org.twins.core.dao.specifications.twinflow.TwinflowSpecification.checkUuidIn;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinflowSearchService {
    private final TwinflowRepository twinflowRepository;
    @Lazy
    private final AuthService authService;
    private final TwinClassService twinClassService;

    public PaginationResult<TwinflowEntity> search(TwinflowSearch twinflowSearch, SimplePagination pagination) throws ServiceException {
        if (twinflowSearch == null)
            twinflowSearch = new TwinflowSearch(); //no filters
        Page<TwinflowEntity> twinflowList = twinflowRepository.findAll(
                createTwinflowEntitySearchSpecification(twinflowSearch),
                PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(twinflowList, pagination);
    }

    private Specification<TwinflowEntity> createTwinflowEntitySearchSpecification(TwinflowSearch search) throws ServiceException {
        Locale locale = authService.getApiUser().getLocale();
        return checkSchemas(TwinflowEntity.Fields.schemaMappings, search.getTwinflowSchemaIdList(), true, false)
                .and(checkSchemas(TwinflowEntity.Fields.schemaMappings, search.getTwinflowSchemaIdExcludeList(), true, true))
                .and(checkUuidIn(search.getIdList(), false, false, TwinflowEntity.Fields.id))
                .and(checkUuidIn(search.getIdExcludeList(), false, false, TwinflowEntity.Fields.id))
                .and(checkUuidIn(twinClassService.loadExtendsHierarchyClasses(search.getTwinClassIdMap()), false, false, TwinflowEntity.Fields.twinClassId))
                .and(checkUuidIn(twinClassService.loadExtendsHierarchyClasses(search.getTwinClassIdExcludeMap()), true, false, TwinflowEntity.Fields.twinClassId))
                .and(joinAndSearchByI18NField(TwinflowEntity.Fields.nameI18n, search.getNameI18nLikeList(), locale, false, false))
                .and(joinAndSearchByI18NField(TwinflowEntity.Fields.nameI18n, search.getNameI18nNotLikeList(), locale, true, true))
                .and(joinAndSearchByI18NField(TwinflowEntity.Fields.descriptionI18n, search.getDescriptionI18nLikeList(), locale, false, false))
                .and(joinAndSearchByI18NField(TwinflowEntity.Fields.descriptionI18n, search.getDescriptionI18nNotLikeList(), locale, false, true))
                .and(checkUuidIn(search.getInitialStatusIdList(), false, false, TwinflowEntity.Fields.initialTwinStatusId))
                .and(checkUuidIn(search.getInitialStatusIdExcludeList(), true, false, TwinflowEntity.Fields.initialTwinStatusId))
                .and(checkUuidIn(search.getCreatedByUserIdList(), false, false, TwinflowEntity.Fields.createdByUserId))
                .and(checkUuidIn(search.getCreatedByUserIdExcludeList(), true, true, TwinflowEntity.Fields.createdByUserId));
    }
}

