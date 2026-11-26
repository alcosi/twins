package org.twins.face.mappers.rest.widget.wt011;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.face.dao.widget.wt011.FaceWT011FieldEntity;
import org.twins.face.dto.rest.widget.wt011.FaceWT011FieldDTOv1;
import org.twins.face.service.widget.FaceWT011FieldService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class FaceWT011FieldRestDTOMapper extends RestSimpleDTOMapper<FaceWT011FieldEntity, FaceWT011FieldDTOv1> {
    private final FaceWT011FieldService faceWT011FieldService;

    @MapperModePointerBinding(modes = FaceWT011Modes.FaceWT011Field2TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @Override
    public void map(FaceWT011FieldEntity src, FaceWT011FieldDTOv1 dst, MapperContext mapperContext) throws Exception {
        faceWT011FieldService.loadTwinClassFields(src);
        dst
                .setId(src.getId())
                .setTwinClassFieldId(src.getTwinClassFieldId())
                .setLabel(I18nCacheHolder.addId(src.getLabelI18nId() != null ? src.getLabelI18nId() : src.getTwinClassField().getNameI18nId()))
                .setOrder(src.getOrder())
                .setWidthWeight(src.getWidthWeight())
                .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "));

        if (mapperContext.hasModeButNot(FaceWT011Modes.FaceWT011Field2TwinClassFieldMode.HIDE)) {
            twinClassFieldRestDTOMapper.postpone(src.getTwinClassField(), mapperContext.forkOnPoint(FaceWT011Modes.FaceWT011Field2TwinClassFieldMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FaceWT011FieldEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceWT011FieldService.loadTwinClassFields(srcCollection);
    }
}
