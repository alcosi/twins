package org.twins.face.mappers.rest.widget.wt001;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.face.FaceWidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.face.dao.widget.wt001.FaceWT001Entity;
import org.twins.face.dto.rest.widget.wt001.FaceWT001DTOv1;
import org.twins.face.service.widget.FaceWT001ColumnService;
import org.twins.face.service.widget.FaceWT001Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FaceMode.class})
public class FaceWT001RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceWT001Entity>, FaceWT001DTOv1> {
    protected final FaceWidgetRestDTOMapper<FaceWT001Entity> faceWidgetRestDTOMapper;
    protected final FaceWT001Service faceWT001Service;
    protected final FaceWT001ColumnService faceWT001ColumnService;

    @MapperModePointerBinding(modes = FaceWT001Modes.FaceWT001Column2TwinClassFieldMode.class)
    protected final FaceWT001ColumnRestDTOMapper faceWT001ColumnRestDTOMapper;

    @MapperModePointerBinding(modes = FaceMode.ModalFace2FaceMode.class)
    protected final FaceRestDTOMapper faceRestDTOMapper;

    @Override
    public void map(PointedFace<FaceWT001Entity> src, FaceWT001DTOv1 dst, MapperContext mapperContext) throws Exception {
        FaceWT001Entity config = src.getConfig();
        faceWidgetRestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setKey(config.getKey());
            case DETAILED -> {
                faceWT001ColumnService.loadColumns(config);
                dst
                        .setKey(config.getKey())
                        .setLabel(I18nCacheHolder.addId(config.getLabelI18nId()))
                        .setTwinClassId(config.getTwinClassId())
                        .setSearchId(config.getSearchId())
                        .setShowCreateButton(config.isShowCreateButton())
                        .setColumns(faceWT001ColumnRestDTOMapper.convertCollection(faceWT001ColumnService.filterVariants(config.getColumns()), mapperContext));
                Map<String, String> searchParams = faceWT001Service.getSearchParams(config);
                if (searchParams != null && !searchParams.isEmpty()) {
                    dst.setSearchParams(searchParams);
                }
            }
        }

        if (mapperContext.hasModeButNot(FaceMode.ModalFace2FaceMode.HIDE)) {
            faceRestDTOMapper.postpone(config.getModalFace(), mapperContext.forkOnPoint(FaceMode.ModalFace2FaceMode.SHORT));
            dst.setModalFaceId(config.getModalFaceId());
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<PointedFace<FaceWT001Entity>> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceWidgetRestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext);
        List<FaceWT001Entity> configs = srcCollection.stream().map(PointedFace::getConfig).toList();
        faceWT001ColumnService.loadColumns(configs);
        if (mapperContext.getModeOrUse(FaceMode.SHORT) == FaceMode.DETAILED) {
            faceWT001Service.loadSearchParams(configs);
        }
    }
}
