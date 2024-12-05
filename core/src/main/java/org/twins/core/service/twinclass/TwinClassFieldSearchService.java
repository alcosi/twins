package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.service.auth.AuthService;

import static org.cambium.i18n.dao.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.twinclass.TwinClassFieldSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldSearchService {
    private final AuthService authService;
    private final TwinClassFieldRepository twinClassFieldRepository;

    public PaginationResult<TwinClassFieldEntity> findTwinClassFieldForDomain(TwinClassFieldSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinClassFieldEntity> spec = createTwinClassFieldSearchSpecification(search);
        Page<TwinClassFieldEntity> ret = twinClassFieldRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<TwinClassFieldEntity> createTwinClassFieldSearchSpecification(TwinClassFieldSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return Specification.where(
                checkDomainId(apiUser.getDomainId())
                        .and(checkUuidIn(TwinClassFieldEntity.Fields.id, search.getIdList(), false, false))
                        .and(checkUuidIn(TwinClassFieldEntity.Fields.id, search.getIdExcludeList(), true, false))
                        .and(checkFieldLikeIn(TwinClassFieldEntity.Fields.key, search.getKeyLikeList(), false, true))
                        .and(checkFieldLikeIn(TwinClassFieldEntity.Fields.key, search.getKeyNotLikeList(), true, true))
                        .and(joinAndSearchByI18NField(TwinClassFieldEntity.Fields.nameI18n, search.getNameI18nLikeList(), apiUser.getLocale(), true, false))
                        .and(joinAndSearchByI18NField(TwinClassFieldEntity.Fields.nameI18n, search.getNameI18nNotLikeList(), apiUser.getLocale(), true, true))
                        .and(joinAndSearchByI18NField(TwinClassFieldEntity.Fields.descriptionI18n, search.getDescriptionI18nLikeList(), apiUser.getLocale(), true, false))
                        .and(joinAndSearchByI18NField(TwinClassFieldEntity.Fields.descriptionI18n, search.getDescriptionI18nNotLikeList(), apiUser.getLocale(), true, true))
                        .and(checkFieldTyperIdIn(search.getFieldTyperIdList(), false, false))
                        .and(checkFieldTyperIdIn(search.getFieldTyperIdExcludeList(), true, true))
                        .and(checkUuidIn(TwinClassFieldEntity.Fields.viewPermissionId, search.getViewPermissionIdList(), false, false))
                        .and(checkUuidIn(TwinClassFieldEntity.Fields.viewPermissionId, search.getViewPermissionIdExcludeList(), true, true))
                        .and(checkUuidIn(TwinClassFieldEntity.Fields.editPermissionId, search.getViewPermissionIdList(), false, false))
                        .and(checkUuidIn(TwinClassFieldEntity.Fields.editPermissionId, search.getViewPermissionIdExcludeList(), true, true))
                        .and(checkRequired(search.getRequired())
        ));
    }
}
