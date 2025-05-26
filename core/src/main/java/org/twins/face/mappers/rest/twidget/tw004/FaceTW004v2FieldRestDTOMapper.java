package org.twins.face.mappers.rest.twidget.tw004;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dto.rest.twidget.tw004.FaceTW004FieldDTOv1;

@Component
@RequiredArgsConstructor
public class FaceTW004v2FieldRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldEntity, FaceTW004FieldDTOv1> {
    private final I18nService i18nService;
    @MapperModePointerBinding(modes = FaceTW004Modes.FaceTW0042TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @Override
    public void map(TwinClassFieldEntity src, FaceTW004FieldDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setLabel(i18nService.translateToLocale(src.getNameI18nId()))
                .setTwinClassFieldId(src.getId())
                .setEditable(true);
        if (mapperContext.hasModeButNot(FaceTW004Modes.FaceTW0042TwinClassFieldMode.HIDE)) {
            twinClassFieldRestDTOMapper.postpone(src, mapperContext.forkOnPoint(FaceTW004Modes.FaceTW0042TwinClassFieldMode.SHORT));
        }
    }
}
