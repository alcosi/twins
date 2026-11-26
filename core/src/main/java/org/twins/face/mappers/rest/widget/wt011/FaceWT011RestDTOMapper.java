package org.twins.face.mappers.rest.widget.wt011;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceWidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.face.dao.widget.wt011.FaceWT011ColumnEntity;
import org.twins.face.dao.widget.wt011.FaceWT011Entity;
import org.twins.face.dto.rest.widget.wt011.FaceWT011DTOv1;
import org.twins.face.service.widget.FaceWT011ColumnService;
import org.twins.face.service.widget.FaceWT011Service;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FaceMode.class})
public class FaceWT011RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceWT011Entity>, FaceWT011DTOv1> {
    protected final FaceWidgetRestDTOMapper<FaceWT011Entity> faceWidgetRestDTOMapper;
    protected final FaceWT011Service faceWT011Service;
    protected final FaceWT011ColumnService faceWT011ColumnService;

    @MapperModePointerBinding(modes = FaceWT011Modes.FaceWT011Field2TwinClassFieldMode.class)
    protected final FaceWT011ColumnRestDTOMapper faceWT011ColumnRestDTOMapper;

    @Override
    public void map(PointedFace<FaceWT011Entity> src, FaceWT011DTOv1 dst, MapperContext mapperContext) throws Exception {
        FaceWT011Entity config = src.getConfig();
        faceWidgetRestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) {
            case SHORT -> dst
                    .setKey(config.getKey());
            case DETAILED -> {
                faceWT011Service.loadColumns(config);
                dst
                        .setKey(config.getKey())
                        .setLabel(I18nCacheHolder.addId(config.getLabelI18nId()))
                        .setSubmitLabel(I18nCacheHolder.addId(config.getSubmitLabelI18nId()))
                        .setCancelLabel(I18nCacheHolder.addId(config.getCancelLabelI18nId()))
                        .setStyleClasses(StringUtils.splitToSet(config.getStyleClasses(), " "))
                        .setColumns(faceWT011ColumnRestDTOMapper.convertCollection(
                                faceWT011ColumnService.filterVariants(config.getColumns(), FaceWT011ColumnEntity::getOrder), mapperContext));
            }
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<PointedFace<FaceWT011Entity>> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceWidgetRestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.getModeOrUse(FaceMode.SHORT) == FaceMode.DETAILED) {
            List<FaceWT011Entity> configs = srcCollection.stream().map(PointedFace::getConfig).toList();
            faceWT011Service.loadColumns(configs);
        }
    }
}
