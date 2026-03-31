package org.twins.core.service.twinclass;

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
import org.twins.core.dao.twinclass.TwinClassFreezeEntity;
import org.twins.core.dao.twinclass.TwinClassFreezeRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.TwinClassFreezeSearch;
import org.twins.core.service.auth.AuthService;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.CommonSpecification.checkFieldLikeIn;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassFreezeSearchService {
    private final TwinClassFreezeRepository twinClassFreezeRepository;
    private final AuthService authService;

    public PaginationResult<TwinClassFreezeEntity> findTwinClassFreezes(TwinClassFreezeSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinClassFreezeEntity> spec = createTwinClassFreezeSearchSpecification(search);
        Page<TwinClassFreezeEntity> ret = twinClassFreezeRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinClassFreezeEntity> createTwinClassFreezeSearchSpecification(TwinClassFreezeSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return Specification.allOf(
                checkUuidIn(search.getIdList(), false, false, TwinClassFreezeEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinClassFreezeEntity.Fields.id),
                checkFieldLikeIn(search.getKeyLikeList(), false, true, TwinClassFreezeEntity.Fields.key),
                checkFieldLikeIn(search.getKeyNotLikeList(), true, true, TwinClassFreezeEntity.Fields.key),
                checkUuidIn(search.getStatusIdList(), false, false, TwinClassFreezeEntity.Fields.twinStatusId),
                checkUuidIn(search.getStatusIdExcludeList(), true, false, TwinClassFreezeEntity.Fields.twinStatusId),
                joinAndSearchByI18NField(TwinClassFreezeEntity.Fields.nameI18NId, search.getNameLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NField(TwinClassFreezeEntity.Fields.nameI18NId, search.getNameNotLikeList(), apiUser.getLocale(), true, true),
                joinAndSearchByI18NField(TwinClassFreezeEntity.Fields.descriptionI18NId, search.getDescriptionLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NField(TwinClassFreezeEntity.Fields.descriptionI18NId, search.getDescriptionNotLikeList(), apiUser.getLocale(), true, true)
        );
    }
}
