package org.twins.face.mappers.rest.widget.wt011;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.face.dao.widget.wt011.FaceWT011ColumnEntity;
import org.twins.face.dao.widget.wt011.FaceWT011SectionEntity;
import org.twins.face.dto.rest.widget.wt011.FaceWT011ColumnDTOv1;
import org.twins.face.service.widget.FaceWT011ColumnService;
import org.twins.face.service.widget.FaceWT011SectionService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class FaceWT011ColumnRestDTOMapper extends RestSimpleDTOMapper<FaceWT011ColumnEntity, FaceWT011ColumnDTOv1> {
    private final FaceWT011ColumnService faceWT011ColumnService;
    private final FaceWT011SectionService faceWT011SectionService;

    @MapperModePointerBinding(modes = FaceWT011Modes.FaceWT011Field2TwinClassFieldMode.class)
    private final FaceWT011SectionRestDTOMapper faceWT011SectionRestDTOMapper;

    @Override
    public void map(FaceWT011ColumnEntity src, FaceWT011ColumnDTOv1 dst, MapperContext mapperContext) throws Exception {
        faceWT011ColumnService.loadSections(src);
        dst
                .setId(src.getId())
                .setOrder(src.getOrder())
                .setWidthWeight(src.getWidthWeight())
                .setAdaptiveOrder(src.getAdaptiveOrder())
                .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "))
                .setSections(faceWT011SectionRestDTOMapper.convertCollection(
                        faceWT011SectionService.filterVariants(src.getSections(), FaceWT011SectionEntity::getOrder), mapperContext));
    }

    @Override
    public void beforeCollectionConversion(Collection<FaceWT011ColumnEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceWT011ColumnService.loadSections(srcCollection);
    }
}
