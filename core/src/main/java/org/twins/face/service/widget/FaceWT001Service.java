package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.holder.TypedRequestCacheHolder;
import org.twins.core.service.context.ContextService;
import org.twins.core.service.face.FaceService;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.widget.wt001.FaceWT001ColumnRepository;
import org.twins.face.dao.widget.wt001.FaceWT001Entity;
import org.twins.face.dao.widget.wt001.FaceWT001Repository;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT001Service extends FaceVariantsService<FaceWT001Entity> {
    /** Request cache prefix (FaceWT001Entity namespace) for search params collected from the widget's context (batch-loaded, no N+1). */
    private static final String SEARCH_PARAMS_CACHE_PREFIX = "searchParams:";

    private final FaceWT001Repository faceWT001Repository;
    private final FaceWT001ColumnRepository faceWT001ColumnRepository;
    private final FaceService faceService;
    private final ContextService contextService;

    @Override
    public CrudRepository<FaceWT001Entity, UUID> entityRepository() {
        return faceWT001Repository;
    }

    @Override
    public Function<FaceWT001Entity, UUID> entityGetIdFunction() {
        return FaceWT001Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT001Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceWT001Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceWT001Entity> getVariants(UUID of) {
        return faceWT001Repository.findByFaceId(of);
    }

    /**
     * Bulk-collects search params from the widgets' contexts ({@code face_wt001.context_id}) for the
     * current request twin — one {@code collectContextsForTwin} call for the whole collection (collector
     * configs shared by several widgets run once) — and puts them into the request cache
     * (see {@link #getSearchParams}). Widgets whose context collected nothing get an EMPTY map cached,
     * so {@link #getSearchParams} will not re-collect them.
     */
    public void loadSearchParams(Collection<FaceWT001Entity> srcCollection) throws ServiceException {
        Set<UUID> contextIds = new HashSet<>();
        for (FaceWT001Entity src : srcCollection) {
            if (src.getContextId() != null) {
                contextIds.add(src.getContextId());
            }
        }
        if (contextIds.isEmpty()) {
            return;
        }
        TwinEntity currentTwin = faceService.getRequestFacePointers().getCurrentTwin();
        if (currentTwin == null) {
            throw new ServiceException(ErrorCodeTwins.POINTER_ON_NULL, "configured search context requires a current twin");
        }
        Map<UUID, Map<String, String>> searchParamsByContextId = contextService.collectContextsForTwin(contextIds, currentTwin);
        for (FaceWT001Entity src : srcCollection) {
            if (src.getContextId() != null) {
                TypedRequestCacheHolder.put(FaceWT001Entity.class, SEARCH_PARAMS_CACHE_PREFIX + src.getId(),
                        searchParamsByContextId.getOrDefault(src.getContextId(), Map.of()));
            }
        }
    }

    /**
     * Search params of the widget from the request cache; on a miss (single-entity convert path —
     * map() runs without beforeCollectionConversion) collects just this widget's params — no batch
     * to leverage.
     */
    public Map<String, String> getSearchParams(FaceWT001Entity src) throws ServiceException {
        if (src.getContextId() == null) {
            return null;
        }
        Map<String, String> searchParams = TypedRequestCacheHolder.get(FaceWT001Entity.class, SEARCH_PARAMS_CACHE_PREFIX + src.getId());
        if (searchParams == null) {
            loadSearchParams(List.of(src));
            searchParams = TypedRequestCacheHolder.get(FaceWT001Entity.class, SEARCH_PARAMS_CACHE_PREFIX + src.getId());
        }
        return searchParams;
    }
}
