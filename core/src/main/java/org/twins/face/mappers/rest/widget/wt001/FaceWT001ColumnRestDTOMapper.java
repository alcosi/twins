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

@Component
@RequiredArgsConstructor
public class FaceWT001ColumnRestDTOMapper extends RestSimpleDTOMapper<FaceWT001ColumnEntity, FaceWT001ColumnDTOv1> {
    @MapperModePointerBinding(modes = FaceWT001Modes.FaceWT001Column2TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @Override
    public void map(FaceWT001ColumnEntity src, FaceWT001ColumnDTOv1 dst, MapperContext mapperContext) throws Exception {
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
}
