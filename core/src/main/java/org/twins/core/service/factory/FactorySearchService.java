package org.twins.core.service.factory;

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
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.FactorySearch;
import org.twins.core.service.auth.AuthService;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NFieldDirect;
import static org.twins.core.dao.specifications.factory.FactorySpecification.*;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class FactorySearchService {
    private final AuthService authService;
    private final TwinFactoryRepository twinFactoryRepository;


    public PaginationResult<TwinFactoryEntity> findFactoriesInDomain(FactorySearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinFactoryEntity> spec = createFactorySearchSpecification(search);
        Page<TwinFactoryEntity> ret = twinFactoryRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinFactoryEntity> createFactorySearchSpecification(FactorySearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return Specification.allOf(
                checkFieldUuid(apiUser.getDomainId(), TwinFactoryEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinFactoryEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinFactoryEntity.Fields.id),
                checkFieldLikeIn(search.getKeyLikeList(), false, false, TwinFactoryEntity.Fields.key),
                checkFieldLikeIn(search.getKeyNotLikeList(), true, true, TwinFactoryEntity.Fields.key),
                joinAndSearchByI18NFieldDirect(TwinFactoryEntity.Fields.nameI18nTranslationsSpecOnly, search.getNameLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NFieldDirect(TwinFactoryEntity.Fields.nameI18nTranslationsSpecOnly, search.getNameNotLikeList(), apiUser.getLocale(), true, true),
                joinAndSearchByI18NFieldDirect(TwinFactoryEntity.Fields.descriptionI18nTranslationsSpecOnly, search.getDescriptionLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NFieldDirect(TwinFactoryEntity.Fields.descriptionI18nTranslationsSpecOnly, search.getDescriptionNotLikeList(), apiUser.getLocale(), true, true));
    }
}
