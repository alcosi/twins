package org.twins.face.mappers.rest.widget.wt002;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dto.rest.widget.wt002.FaceWT002ButtonFieldDTOv1;
import org.twins.face.mappers.rest.twidget.tw004.FaceTW004Modes;

@Component
@RequiredArgsConstructor
public class FaceWT002ButtonFieldRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldEntity, FaceWT002ButtonFieldDTOv1> {
    private final I18nService i18nService;

    @MapperModePointerBinding(modes = FaceTW004Modes.FaceTW0042TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @Override
    public void map(TwinClassFieldEntity src, FaceWT002ButtonFieldDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setLabel(i18nService.translateToLocale(src.getNameI18nId()))
                .setTwinClassFieldId(src.getId());
        if (mapperContext.hasModeButNot(FaceWT002Modes.FaceTW002Button2TwinClassFieldMode.HIDE)) {
            twinClassFieldRestDTOMapper.postpone(src, mapperContext.forkOnPoint(FaceWT002Modes.FaceTW002Button2TwinClassFieldMode.SHORT));
        }
    }
}
