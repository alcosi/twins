package org.twins.face.mappers.rest.widget.wt006;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.face.domain.widget.wt006.FaceWT006TwinClassField;
import org.twins.face.dto.rest.widget.wt006.FaceWT006FieldDTOv1;

@Component
@RequiredArgsConstructor
public class FaceWT006v2FieldRestDTOMapper extends RestSimpleDTOMapper<FaceWT006TwinClassField, FaceWT006FieldDTOv1> {
    @MapperModePointerBinding(modes = FaceWT006Modes.FaceWT0062TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @Override
    public void map(FaceWT006TwinClassField src, FaceWT006FieldDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getField().getKey())
                .setLabel(I18nCacheHolder.addId(src.getField().getNameI18nId()))
                .setTwinClassFieldId(src.getField().getId())
                .setEditable(src.isEditable())
                .setOrder(src.getOrder());
        if (mapperContext.hasModeButNot(FaceWT006Modes.FaceWT0062TwinClassFieldMode.HIDE)) {
            twinClassFieldRestDTOMapper.postpone(src.getField(), mapperContext.forkOnPoint(FaceWT006Modes.FaceWT0062TwinClassFieldMode.SHORT));
        }
    }
}
