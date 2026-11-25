package org.twins.face.mappers.rest.widget.wt001;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.face.dao.widget.wt001.FaceWT001ColumnEntity;
import org.twins.face.dto.rest.widget.wt001.FaceWT001ColumnDTOv1;
import org.twins.face.service.widget.FaceWT001ColumnService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class FaceWT001ColumnRestDTOMapper extends RestSimpleDTOMapper<FaceWT001ColumnEntity, FaceWT001ColumnDTOv1> {
    @MapperModePointerBinding(modes = FaceWT001Modes.FaceWT001Column2TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    private final FaceWT001ColumnService faceWT001ColumnService;

    @Override
    public void map(FaceWT001ColumnEntity src, FaceWT001ColumnDTOv1 dst, MapperContext mapperContext) throws Exception {
        faceWT001ColumnService.loadTwinClassFields(src);
        dst
                .setId(src.getId())
                .setLabel(I18nCacheHolder.addId(src.getLabelI18nId() != null ? src.getLabelI18nId() : src.getTwinClassField().getNameI18nId()))
                .setOrder(src.getOrder())
                .setShowByDefault(src.getShowByDefault())
                .setTwinClassFieldId(src.getTwinClassFieldId());

        if (mapperContext.hasModeButNot(FaceWT001Modes.FaceWT001Column2TwinClassFieldMode.HIDE)) {
            twinClassFieldRestDTOMapper.postpone(src.getTwinClassField(), mapperContext.forkOnPoint(FaceWT001Modes.FaceWT001Column2TwinClassFieldMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FaceWT001ColumnEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceWT001ColumnService.loadTwinClassFields(srcCollection);
    }
}
