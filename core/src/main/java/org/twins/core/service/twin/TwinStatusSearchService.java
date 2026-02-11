package org.twins.core.service.twin;

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
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.TwinStatusSearch;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.CommonSpecification.*;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinStatusSearchService {
    private final TwinStatusRepository twinStatusRepository;
    private final AuthService authService;
    private final TwinClassService twinClassService;

    public PaginationResult<TwinStatusEntity> findTwinStatuses(TwinStatusSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinStatusEntity> spec = createTwinStatusSearchSpecification(search);
        Page<TwinStatusEntity> ret = twinStatusRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinStatusEntity> createTwinStatusSearchSpecification(TwinStatusSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return Specification.allOf(
                checkFieldUuid(apiUser.getDomainId(), TwinStatusEntity.Fields.twinClass, TwinClassEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinStatusEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinStatusEntity.Fields.id),
                checkUuidIn(twinClassService.loadExtendsHierarchyClasses(search.getTwinClassIdMap()), false, false, TwinStatusEntity.Fields.twinClassId),
                checkUuidIn(twinClassService.loadExtendsHierarchyClasses(search.getTwinClassIdExcludeMap()), true, false, TwinStatusEntity.Fields.twinClassId),
                checkFieldLikeIn(search.getKeyLikeList(), false, true, TwinStatusEntity.Fields.key),
                checkFieldLikeIn(search.getKeyNotLikeList(), true, true, TwinStatusEntity.Fields.key),
                joinAndSearchByI18NField(TwinStatusEntity.Fields.nameI18n, search.getNameI18nLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NField(TwinStatusEntity.Fields.nameI18n, search.getNameI18nNotLikeList(), apiUser.getLocale(), true, true),
                joinAndSearchByI18NField(TwinStatusEntity.Fields.descriptionI18n, search.getDescriptionI18nLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NField(TwinStatusEntity.Fields.descriptionI18n, search.getDescriptionI18nNotLikeList(), apiUser.getLocale(), true, true)
        );
    }
}
