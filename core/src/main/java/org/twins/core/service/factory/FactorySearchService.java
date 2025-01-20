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
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.FactorySearch;
import org.twins.core.service.auth.AuthService;

import static org.cambium.i18n.dao.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.factory.FactorySpecification.*;


@Slf4j
@Service
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
                checkUuidIn(TwinFactoryEntity.Fields.id, search.getIdList(), false, false),
                checkUuidIn(TwinFactoryEntity.Fields.id, search.getIdExcludeList(), true, false),
                checkFieldLikeIn(TwinFactoryEntity.Fields.key, search.getKeyLikeList(), false, false),
                checkFieldLikeIn(TwinFactoryEntity.Fields.key, search.getKeyNotLikeList(), true, true),
                joinAndSearchByI18NField(TwinFactoryEntity.Fields.nameI18n, search.getNameLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NField(TwinFactoryEntity.Fields.nameI18n, search.getNameNotLikeList(), apiUser.getLocale(), true, true),
                joinAndSearchByI18NField(TwinFactoryEntity.Fields.descriptionI18n, search.getDescriptionLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NField(TwinFactoryEntity.Fields.descriptionI18n, search.getDescriptionNotLikeList(), apiUser.getLocale(), true, true));
    }
}
