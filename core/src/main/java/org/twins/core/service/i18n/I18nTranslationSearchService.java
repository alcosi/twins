package org.twins.core.service.i18n;

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
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.i18n.I18nTranslationRepository;
import org.twins.core.domain.search.I18nTranslationSearch;
import org.twins.core.service.auth.AuthService;

import java.util.Collections;
import java.util.List;

import static org.twins.core.dao.i18n.specifications.I18nTranslationSpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class I18nTranslationSearchService {
    private final I18nTranslationRepository repository;
    private final AuthService authService;

    public List<I18nTranslationEntity> findI18nTranslations(I18nTranslationSearch search) throws ServiceException {
        Specification<I18nTranslationEntity> spec = createI18nTranslationSpecification(search);
        return repository.findAll(spec);
    }

    public PaginationResult<I18nTranslationEntity> findI18nTranslations(I18nTranslationSearch search, SimplePagination pagination) throws ServiceException {
        Specification<I18nTranslationEntity> spec = createI18nTranslationSpecification(search);
        Page<I18nTranslationEntity> ret = repository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<I18nTranslationEntity> createI18nTranslationSpecification(I18nTranslationSearch search) throws ServiceException {
        return Specification.allOf(
                checkUuidIn(Collections.singletonList(authService.getApiUser().getDomainId()), false, true, I18nTranslationEntity.Fields.i18n, I18nEntity.Fields.domainId),
                checkUuidIn(search.getI18nIdList(), false, false, I18nTranslationEntity.Fields.i18nId),
                checkUuidIn(search.getI18nIdExcludeList(), true, false, I18nTranslationEntity.Fields.i18nId),
                checkFieldLikeIn(search.getTranslationLikeList(), false, true, I18nTranslationEntity.Fields.translation),
                checkFieldLikeIn(search.getTranslationNotLikeList(), true, true, I18nTranslationEntity.Fields.translation),
                checkFieldLongRange(search.getUsageCounter(), I18nTranslationEntity.Fields.usageCounter),
                checkLocaleIn(search.getLocaleLikeList(), false),
                checkLocaleIn(search.getLocaleNotLikeList(), true)
        );
    }
}
