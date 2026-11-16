package org.twins.core.service.comment;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.comment.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.CommentSearch;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.featurer.comment.finder.CommentFinder;
import org.twins.core.featurer.comment.sorter.CommentSorter;
import org.twins.core.featurer.search.SearchFinder;
import org.twins.core.service.ConfiguredSearchService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.cambium.common.util.SetUtils.narrowSet;

@RequiredArgsConstructor
@Service
public class CommentSearchConfigService extends ConfiguredSearchService<TwinCommentSearchEntity, TwinCommentSearchPredicateEntity, CommentSearch, TwinCommentEntity> {
    private final TwinCommentSearchRepository twinCommentSearchRepository;
    private final TwinCommentSearchPredicateRepository twinCommentSearchPredicateRepository;
    private final TwinCommentRepository twinCommentRepository;
    private final CommentSearchService commentSearchService;

    @Override
    public CrudRepository<TwinCommentSearchEntity, UUID> entityRepository() {
        return twinCommentSearchRepository;
    }

    @Override
    public Function<TwinCommentSearchEntity, UUID> entityGetIdFunction() {
        return TwinCommentSearchEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinCommentSearchEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return checkDomainAccessDenied(entity.getDomainId(), entity.logNormal(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(TwinCommentSearchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    protected UUID unlimitedSearchId() {
        return SystemIds.TwinCommentSearch.UNLIMITED;
    }

    @Override
    protected CommentSearch emptySearch() {
        return new CommentSearch();
    }

    @Override
    protected List<TwinCommentSearchPredicateEntity> loadPredicates(TwinCommentSearchEntity searchEntity) {
        return twinCommentSearchPredicateRepository.findByTwinCommentSearchId(searchEntity.getId());
    }

    @Override
    protected Class<? extends SearchFinder<CommentSearch>> finderClass() {
        return CommentFinder.class;
    }

    @Override
    protected PaginationResult<TwinCommentEntity> runFreeSearch(CommentSearch search, SimplePagination pagination) throws ServiceException {
        return commentSearchService.search(search, pagination);
    }

    @Override
    protected PaginationResult<TwinCommentEntity> runConfiguredSearch(CommentSearch mainSearch, SimplePagination pagination) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Specification<TwinCommentEntity> spec = commentSearchService.createFilterSpecification(mainSearch, apiUser.getDomainId(), apiUser.getLocale());
        spec = addConfiguredSorting(mainSearch, pagination, spec, CommentSorter.class);
        Page<TwinCommentEntity> page = twinCommentRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(page, pagination);
    }

    @Override
    protected void narrowSearch(CommentSearch mainSearch, CommentSearch narrowSearch) {
        if (narrowSearch == null) {
            return;
        }
        for (Pair<Function<CommentSearch, Set>, BiConsumer<CommentSearch, Set>> functionPair : CommentSearch.SET_FIELDS) {
            Set mainSet = functionPair.getKey().apply(mainSearch);
            Set narrowSet = functionPair.getKey().apply(narrowSearch);
            functionPair.getValue().accept(mainSearch, narrowSet(mainSet, narrowSet));
        }
    }
}
