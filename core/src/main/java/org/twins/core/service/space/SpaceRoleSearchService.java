package org.twins.core.service.space;

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
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.dao.space.SpaceRoleRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.SpaceRoleSearch;
import org.twins.core.service.auth.AuthService;

import java.util.Set;

import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.space.SpaceRoleSpecification.checkFieldLikeIn;
import static org.twins.core.dao.specifications.space.SpaceRoleSpecification.checkUuidIn;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class SpaceRoleSearchService {
    private final AuthService authService;
    private final SpaceRoleRepository spaceRoleRepository;

    public PaginationResult<SpaceRoleEntity> findSpaceRole(SpaceRoleSearch search, SimplePagination pagination) throws ServiceException {
        Specification<SpaceRoleEntity> spec = createSpaceRoleSearchSpecification(search);
        Page<SpaceRoleEntity> ret = spaceRoleRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    private Specification<SpaceRoleEntity> createSpaceRoleSearchSpecification(SpaceRoleSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return Specification.allOf(
                checkUuidIn(Set.of(apiUser.getDomainId()), false, false, SpaceRoleEntity.Fields.twinClass, TwinClassEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, true, SpaceRoleEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, SpaceRoleEntity.Fields.id),
                checkUuidIn(search.getTwinClassIdList(), false, true, SpaceRoleEntity.Fields.twinClassId),
                checkUuidIn(search.getTwinClassIdExcludeList(), true, false, SpaceRoleEntity.Fields.twinClassId),
                checkUuidIn(search.getBusinessAccountIdList(), false, true, SpaceRoleEntity.Fields.businessAccountId),
                checkUuidIn(search.getBusinessAccountIdExcludeList(), true, false, SpaceRoleEntity.Fields.businessAccountId),
                checkFieldLikeIn(search.getKeyLikeList(), false, true, SpaceRoleEntity.Fields.key),
                checkFieldLikeIn(search.getKeyNotLikeList(), true, true, SpaceRoleEntity.Fields.key),
                joinAndSearchByI18NField(SpaceRoleEntity.Fields.nameI18n, search.getNameI18nLikeList(), apiUser.getLocale(), false, false),
                joinAndSearchByI18NField(SpaceRoleEntity.Fields.nameI18n, search.getNameI18nNotLikeList(), apiUser.getLocale(), true, true),
                joinAndSearchByI18NField(SpaceRoleEntity.Fields.descriptionI18n, search.getDescriptionI18nLikeList(), apiUser.getLocale(), false, false),
                joinAndSearchByI18NField(SpaceRoleEntity.Fields.descriptionI18n, search.getDescriptionI18nNotLikeList(), apiUser.getLocale(), true, true)
        );
    }
}
