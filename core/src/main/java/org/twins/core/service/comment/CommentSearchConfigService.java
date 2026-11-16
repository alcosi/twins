package org.twins.core.service.comment;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
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
import org.twins.core.service.auth.AuthService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.cambium.common.util.SetUtils.narrowSet;

@RequiredArgsConstructor
@Service
public class CommentSearchConfigService extends EntitySecureFindServiceImpl<TwinCommentSearchEntity> {
    private final AuthService authService;
    private final TwinCommentSearchRepository twinCommentSearchRepository;
    private final TwinCommentSearchPredicateRepository twinCommentSearchPredicateRepository;
    private final TwinCommentRepository twinCommentRepository;
    private final CommentSearchService commentSearchService;
    private final FeaturerService featurerService;

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

    public PaginationResult<TwinCommentEntity> findComments(UUID searchId, Map<String, String> namedParamsMap, CommentSearch narrowSearch, SimplePagination pagination) throws ServiceException {
        if (SystemIds.TwinCommentSearch.UNLIMITED.equals(searchId)) {
            return commentSearchService.search(narrowSearch, pagination);
        }

        TwinCommentSearchEntity searchEntity = findEntitySafe(searchId);
        List<TwinCommentSearchPredicateEntity> searchPredicates = twinCommentSearchPredicateRepository.findByTwinCommentSearchId(searchEntity.getId());

        CommentSearch mainSearch = new CommentSearch();
        for (TwinCommentSearchPredicateEntity predicate : searchPredicates) {
            CommentFinder commentFinder = featurerService.getFeaturer(predicate.getCommentFinderFeaturerId(), CommentFinder.class);
            commentFinder.concatSearch(predicate.getCommentFinderParams(), mainSearch, namedParamsMap);
        }

        narrowSearch(mainSearch, narrowSearch);
        mainSearch.setConfiguredSearch(searchEntity);

        return findCommentsConfigured(mainSearch, pagination);
    }

    private PaginationResult<TwinCommentEntity> findCommentsConfigured(CommentSearch search, SimplePagination pagination) throws ServiceException {
        if (search == null)
            search = new CommentSearch();
        ApiUser apiUser = authService.getApiUser();
        Specification<TwinCommentEntity> spec = commentSearchService.createFilterSpecification(search, apiUser.getDomainId(), apiUser.getLocale());
        spec = addConfiguredSorting(search, pagination, spec);
        Page<TwinCommentEntity> page = twinCommentRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(page, pagination);
    }

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

    private Specification<TwinCommentEntity> addConfiguredSorting(CommentSearch search, SimplePagination pagination, Specification<TwinCommentEntity> spec) throws ServiceException {
        TwinCommentSearchEntity searchEntity = search.getConfiguredSearch();
        if (searchEntity != null && (searchEntity.getForceSorting() || pagination == null || pagination.getSort() == null)) {
            CommentSorter commentSorter = featurerService.getFeaturer(searchEntity.getCommentSorterFeaturerId(), CommentSorter.class);
            var sortFunction = commentSorter.createSort(searchEntity.getCommentSorterParams());
            if (sortFunction != null) {
                spec = sortFunction.apply(spec);
                if (pagination != null) {
                    pagination.setSort(null);
                }
            }
        }
        return spec;
    }
}
