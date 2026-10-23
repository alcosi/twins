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
     * configs shared by several widgets run once).
     *
     * @return widget id → collected params; widgets with a contextId that collected nothing map to an
     * EMPTY map (lets the caller cache "no data" and skip re-collection); widgets without a contextId
     * are absent
     */
    public Map<UUID, Map<String, String>> collectSearchParams(Collection<FaceWT001Entity> srcCollection) throws ServiceException {
        Set<UUID> contextIds = new HashSet<>();
        for (FaceWT001Entity src : srcCollection) {
            if (src.getContextId() != null) {
                contextIds.add(src.getContextId());
            }
        }
        if (contextIds.isEmpty()) {
            return Map.of();
        }
        TwinEntity currentTwin = faceService.getRequestFacePointers().getCurrentTwin();
        if (currentTwin == null) {
            throw new ServiceException(ErrorCodeTwins.POINTER_ON_NULL, "configured search context requires a current twin");
        }
        Map<UUID, Map<String, String>> searchParamsByContextId = contextService.collectContextsForTwin(contextIds, currentTwin);
        Map<UUID, Map<String, String>> result = new HashMap<>();
        for (FaceWT001Entity src : srcCollection) {
            if (src.getContextId() != null) {
                result.put(src.getId(), searchParamsByContextId.getOrDefault(src.getContextId(), Map.of()));
            }
        }
        return result;
    }
}
