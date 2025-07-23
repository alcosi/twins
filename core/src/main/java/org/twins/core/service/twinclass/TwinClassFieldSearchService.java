package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.MapUtils;
import org.cambium.common.util.PaginationUtils;
import org.cambium.common.util.Ternary;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.twinclass.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.featurer.fieldfinder.FieldFinder;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.cambium.common.util.SetUtils.narrowSet;
import static org.twins.core.dao.i18n.specifications.I18nSpecification.joinAndSearchByI18NField;
import static org.twins.core.dao.specifications.twinclass.TwinClassFieldSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldSearchService extends EntitySecureFindServiceImpl<TwinClassFieldSearchEntity> {

    private final TwinClassFieldRepository twinClassFieldRepository;
    private final TwinClassFieldSearchRepository fieldSearchRepository;
    private final TwinClassFieldSearchPredicateRepository fieldSearchPredicateRepository;

    private final AuthService authService;
    private final TwinClassService twinClassService;
    private final FeaturerService featurerService;

    public PaginationResult<TwinClassFieldEntity> findTwinClassField(TwinClassFieldSearch search, SimplePagination pagination) throws ServiceException {
        Specification<TwinClassFieldEntity> spec = createTwinClassFieldSearchSpecification(search);
        Page<TwinClassFieldEntity> ret = twinClassFieldRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }


    public List<TwinClassFieldEntity> findTwinClassField(TwinClassFieldSearch search) throws ServiceException {
        Specification<TwinClassFieldEntity> spec = createTwinClassFieldSearchSpecification(search);
        List<TwinClassFieldEntity> result = twinClassFieldRepository.findAll(spec);
        return result;
    }

    public PaginationResult<TwinClassFieldEntity> findTwinClassField(UUID searchId, TwinClassFieldSearch narrowSearch, SimplePagination pagination) throws ServiceException {
        if (SystemEntityService.TWIN_CLASS_FIELD_SEARCH_UNLIMITED.equals(searchId)) {
            return findTwinClassField(narrowSearch, pagination);
        }
        TwinClassFieldSearchEntity searchEntity = findEntitySafe(searchId);
        List<TwinClassFieldSearchPredicateEntity> searchPredicates = fieldSearchPredicateRepository.findByTwinClassFieldSearchId(searchEntity.getId());
        TwinClassFieldSearch mainSearch = new TwinClassFieldSearch()
                .setExcludeSystemFields(false);
        for(TwinClassFieldSearchPredicateEntity predicate: searchPredicates) {
            FieldFinder fieldFinder = featurerService.getFeaturer(predicate.getFieldFinderFeaturerId(), FieldFinder.class);
            fieldFinder.concatSearch(predicate.getFieldFinderParams(), mainSearch);
        }
        narrowSearch(mainSearch, narrowSearch);
        return findTwinClassField(mainSearch, pagination);
    }

    private Specification<TwinClassFieldEntity> createTwinClassFieldSearchSpecification(TwinClassFieldSearch search) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return Specification.allOf(
                checkUuid(apiUser.getDomainId(), false, !search.isExcludeSystemFields(), TwinClassFieldEntity.Fields.twinClass, TwinClassEntity.Fields.domainId),
                checkUuidIn(search.getIdList(), false, false, TwinClassFieldEntity.Fields.id),
                checkUuidIn(search.getIdExcludeList(), true, false, TwinClassFieldEntity.Fields.id),
                checkUuidIn(twinClassService.loadExtendsHierarchyClasses(search.getTwinClassIdMap()), false, false, TwinClassFieldEntity.Fields.twinClassId),
                checkUuidIn(twinClassService.loadExtendsHierarchyClasses(search.getTwinClassIdExcludeMap()), true, false, TwinClassFieldEntity.Fields.twinClassId),
                checkFieldLikeIn(search.getKeyLikeList(), false, true, TwinClassFieldEntity.Fields.key),
                checkFieldLikeIn(search.getKeyNotLikeList(), true, true, TwinClassFieldEntity.Fields.key),
                joinAndSearchByI18NField(TwinClassFieldEntity.Fields.nameI18n, search.getNameI18nLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NField(TwinClassFieldEntity.Fields.nameI18n, search.getNameI18nNotLikeList(), apiUser.getLocale(), true, true),
                joinAndSearchByI18NField(TwinClassFieldEntity.Fields.descriptionI18n, search.getDescriptionI18nLikeList(), apiUser.getLocale(), true, false),
                joinAndSearchByI18NField(TwinClassFieldEntity.Fields.descriptionI18n, search.getDescriptionI18nNotLikeList(), apiUser.getLocale(), true, true),
                checkFieldTyperIdIn(search.getFieldTyperIdList(), false, false),
                checkFieldTyperIdIn(search.getFieldTyperIdExcludeList(), true, true),
                checkUuidIn(search.getViewPermissionIdList(), false, false, TwinClassFieldEntity.Fields.viewPermissionId),
                checkUuidIn(search.getViewPermissionIdExcludeList(), true, true, TwinClassFieldEntity.Fields.viewPermissionId),
                checkUuidIn(search.getViewPermissionIdList(), false, false, TwinClassFieldEntity.Fields.editPermissionId),
                checkUuidIn(search.getViewPermissionIdExcludeList(), true, true, TwinClassFieldEntity.Fields.editPermissionId),
                checkTernary(search.getRequired(), TwinClassFieldEntity.Fields.required),
                checkFieldLikeIn(search.getExternalIdLikeList(), false, true, TwinClassFieldEntity.Fields.externalId),
                checkFieldLikeIn(search.getExternalIdNotLikeList(), true, true, TwinClassFieldEntity.Fields.externalId));
    }

    @Override
    public CrudRepository<TwinClassFieldSearchEntity, UUID> entityRepository() {
        return fieldSearchRepository;
    }

    @Override
    public Function<TwinClassFieldSearchEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldSearchEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldSearchEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = entity.getDomainId() != null && !entity.getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.logNormal() + " is not allowed in" + domain.logShort());
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(TwinClassFieldSearchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    protected void narrowSearch(TwinClassFieldSearch mainSearch, TwinClassFieldSearch narrowSearch) {
        if (narrowSearch == null)
            return;
        for (Pair<Function<TwinClassFieldSearch, Set>, BiConsumer<TwinClassFieldSearch, Set>> functioPair : TwinClassFieldSearch.FUNCTIONS) {
            Set mainSet = functioPair.getKey().apply(mainSearch);
            Set narrowSet = functioPair.getKey().apply(narrowSearch);
            functioPair.getValue().accept(mainSearch, narrowSet(mainSet, narrowSet));
        }
        if (mainSearch.getRequired() == null || mainSearch.getRequired() == Ternary.ANY) {
            mainSearch.setRequired(narrowSearch.getRequired());
        } //else narrow by required will be ignored

        mainSearch.setTwinClassIdMap(MapUtils.narrowMapOfBooleans(mainSearch.getTwinClassIdMap(), narrowSearch.getTwinClassIdMap(), Boolean.TRUE));
        mainSearch.setTwinClassIdExcludeMap(MapUtils.narrowMapOfBooleans(mainSearch.getTwinClassIdExcludeMap(), narrowSearch.getTwinClassIdExcludeMap(), Boolean.TRUE));

    }
}
