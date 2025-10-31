package org.twins.face.mappers.rest.twidget.tw004;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.face.domain.twidget.tw004.FaceTW004TwinClassField;
import org.twins.face.dto.rest.twidget.tw004.FaceTW004FieldDTOv1;

@Component
@RequiredArgsConstructor
public class FaceTW004v2FieldRestDTOMapper extends RestSimpleDTOMapper<FaceTW004TwinClassField, FaceTW004FieldDTOv1> {
    @MapperModePointerBinding(modes = FaceTW004Modes.FaceTW0042TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @Override
    public void map(FaceTW004TwinClassField src, FaceTW004FieldDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getField().getKey())
                .setLabel(I18nCacheHolder.addId(src.getField().getNameI18nId()))
                .setTwinClassFieldId(src.getField().getId())
                .setEditable(src.isEditable())
                .setOrder(src.getOrder());
        if (mapperContext.hasModeButNot(FaceTW004Modes.FaceTW0042TwinClassFieldMode.HIDE)) {
            twinClassFieldRestDTOMapper.postpone(src.getField(), mapperContext.forkOnPoint(FaceTW004Modes.FaceTW0042TwinClassFieldMode.SHORT));
        }
    }
}
