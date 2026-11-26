package org.twins.face.mappers.rest.widget.wt011;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.face.dao.widget.wt011.FaceWT011FieldEntity;
import org.twins.face.dao.widget.wt011.FaceWT011RowEntity;
import org.twins.face.dto.rest.widget.wt011.FaceWT011RowDTOv1;
import org.twins.face.service.widget.FaceWT011FieldService;
import org.twins.face.service.widget.FaceWT011RowService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class FaceWT011RowRestDTOMapper extends RestSimpleDTOMapper<FaceWT011RowEntity, FaceWT011RowDTOv1> {
    private final FaceWT011RowService faceWT011RowService;
    private final FaceWT011FieldService faceWT011FieldService;

    @MapperModePointerBinding(modes = FaceWT011Modes.FaceWT011Field2TwinClassFieldMode.class)
    private final FaceWT011FieldRestDTOMapper faceWT011FieldRestDTOMapper;

    @Override
    public void map(FaceWT011RowEntity src, FaceWT011RowDTOv1 dst, MapperContext mapperContext) throws Exception {
        faceWT011RowService.loadFields(src);
        dst
                .setId(src.getId())
                .setOrder(src.getOrder())
                .setAllowWrap(src.getAllowWrap())
                .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "))
                .setFields(faceWT011FieldRestDTOMapper.convertCollection(
                        faceWT011FieldService.filterVariants(src.getFields(), FaceWT011FieldEntity::getOrder), mapperContext));
    }

    @Override
    public void beforeCollectionConversion(Collection<FaceWT011RowEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceWT011RowService.loadFields(srcCollection);
    }
}
