package org.twins.face.mappers.rest.widget.wt001;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.face.dao.widget.wt001.FaceWT001Entity;
import org.twins.face.dto.rest.widget.wt001.FaceWT001DTOv1;
import org.twins.face.service.widget.FaceWT001ColumnService;
import org.twins.face.service.widget.FaceWT001Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FaceMode.class})
public class FaceWT001RestDTOMapper extends RestSimpleDTOMapper<FaceWT001Entity, FaceWT001DTOv1> {
    /** MapperContext cache prefix (FaceWT001Entity namespace) for search params collected from the widget's context, preloaded per batch (no N+1). */
    private static final String SEARCH_PARAMS_CACHE_PREFIX = "searchParams:";

    protected final FaceWT001Service faceWT001Service;
    protected final FaceWT001ColumnService faceWT001ColumnService;

    @MapperModePointerBinding(modes = FaceWT001Modes.FaceWT001Column2TwinClassFieldMode.class)
    protected final FaceWT001ColumnRestDTOMapper faceWT001ColumnRestDTOMapper;

    @MapperModePointerBinding(modes = FaceMode.ModalFace2FaceMode.class)
    protected final FaceRestDTOMapper faceRestDTOMapper;

    @Override
    public void map(FaceWT001Entity src, FaceWT001DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setKey(src.getKey());
            case DETAILED -> {
                faceWT001ColumnService.loadColumns(src);
                dst
                        .setKey(src.getKey())
                        .setLabel(I18nCacheHolder.addId(src.getLabelI18nId()))
                        .setTwinClassId(src.getTwinClassId())
                        .setSearchId(src.getSearchId())
                        .setShowCreateButton(src.isShowCreateButton())
                        .setColumns(faceWT001ColumnRestDTOMapper.convertCollection(faceWT001ColumnService.filterVariants(src.getColumns()), mapperContext));
                Map<String, String> searchParams = getSearchParams(src, mapperContext);
                if (searchParams != null && !searchParams.isEmpty()) {
                    dst.setSearchParams(searchParams);
                }
            }
        }

        if (mapperContext.hasModeButNot(FaceMode.ModalFace2FaceMode.HIDE)) {
            faceRestDTOMapper.postpone(src.getModalFace(), mapperContext.forkOnPoint(FaceMode.ModalFace2FaceMode.SHORT));
            dst.setModalFaceId(src.getModalFaceId());
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FaceWT001Entity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceWT001ColumnService.loadColumns(srcCollection);
        if (mapperContext.getModeOrUse(FaceMode.SHORT) == FaceMode.DETAILED) {
            Map<UUID, Map<String, String>> collected = faceWT001Service.collectSearchParams(srcCollection);
            for (var entry : collected.entrySet()) {
                mapperContext.putToCache(FaceWT001Entity.class, SEARCH_PARAMS_CACHE_PREFIX + entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Search params from the batch cache; on a miss (single-entity convert path — map() runs without
     * beforeCollectionConversion) collects just this widget's params — no batch to leverage.
     */
    private Map<String, String> getSearchParams(FaceWT001Entity src, MapperContext mapperContext) throws ServiceException {
        if (src.getContextId() == null) {
            return null;
        }
        Map<String, String> searchParams = mapperContext.getFromCache(FaceWT001Entity.class, SEARCH_PARAMS_CACHE_PREFIX + src.getId());
        if (searchParams == null) {
            searchParams = faceWT001Service.collectSearchParams(List.of(src)).get(src.getId());
        }
        return searchParams;
    }
}
