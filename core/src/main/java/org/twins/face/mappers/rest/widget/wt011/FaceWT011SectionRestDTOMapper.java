package org.twins.face.mappers.rest.widget.wt011;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.face.dao.widget.wt011.FaceWT011RowEntity;
import org.twins.face.dao.widget.wt011.FaceWT011SectionEntity;
import org.twins.face.dto.rest.widget.wt011.FaceWT011SectionDTOv1;
import org.twins.face.service.widget.FaceWT011RowService;
import org.twins.face.service.widget.FaceWT011SectionService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class FaceWT011SectionRestDTOMapper extends RestSimpleDTOMapper<FaceWT011SectionEntity, FaceWT011SectionDTOv1> {
    private final FaceWT011SectionService faceWT011SectionService;
    private final FaceWT011RowService faceWT011RowService;

    @MapperModePointerBinding(modes = FaceWT011Modes.FaceWT011Field2TwinClassFieldMode.class)
    private final FaceWT011RowRestDTOMapper faceWT011RowRestDTOMapper;

    @Override
    public void map(FaceWT011SectionEntity src, FaceWT011SectionDTOv1 dst, MapperContext mapperContext) throws Exception {
        faceWT011SectionService.loadRows(src);
        dst
                .setId(src.getId())
                .setLabel(I18nCacheHolder.addId(src.getLabelI18nId()))
                .setOrder(src.getOrder())
                .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "))
                .setRows(faceWT011RowRestDTOMapper.convertCollection(
                        faceWT011RowService.filterVariants(src.getRows(), FaceWT011RowEntity::getOrder), mapperContext));
    }

    @Override
    public void beforeCollectionConversion(Collection<FaceWT011SectionEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceWT011SectionService.loadRows(srcCollection);
    }
}
