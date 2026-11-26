package org.twins.core.service.twinclassfield;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.*;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.enums.sort.TwinClassFieldSortField;
import org.twins.core.featurer.classfield.finder.FieldFinder;
import org.twins.core.featurer.classfield.sorter.FieldSorter;
import org.twins.core.featurer.search.SearchFinder;
import org.twins.core.featurer.search.SearchSorter;
import org.twins.core.service.ConfiguredSearchService;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldSearchConfigService extends ConfiguredSearchService<TwinClassFieldSearchEntity, TwinClassFieldSearchPredicateEntity, TwinClassFieldSearch, TwinClassFieldEntity, TwinClassFieldSortField> {
    private final TwinClassFieldSearchRepository fieldSearchRepository;
    private final TwinClassFieldSearchPredicateRepository fieldSearchPredicateRepository;
    private final TwinClassFieldRepository twinClassFieldRepository;
    private final TwinClassFieldSearchService twinClassFieldSearchService;

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
        return checkDomainAccessDenied(entity.getDomainId(), entity.logNormal(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(TwinClassFieldSearchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    protected UUID unlimitedSearchId() {
        return SystemIds.TwinClassFieldSearch.UNLIMITED;
    }

    @Override
    protected List<TwinClassFieldSearchPredicateEntity> loadPredicates(TwinClassFieldSearchEntity searchEntity) {
        return fieldSearchPredicateRepository.findByTwinClassFieldSearchId(searchEntity.getId());
    }

    @Override
    protected Class<? extends SearchFinder<TwinClassFieldSearch>> finderClass() {
        return FieldFinder.class;
    }

    @Override
    protected Class<? extends SearchSorter<TwinClassFieldEntity>> sorterClass() {
        return FieldSorter.class;
    }

    @Override
    protected JpaSpecificationExecutor<TwinClassFieldEntity> searchExecutor() {
        return twinClassFieldRepository;
    }

    @Override
    protected TwinClassFieldSearchService entitySearchService() {
        return twinClassFieldSearchService;
    }
}
