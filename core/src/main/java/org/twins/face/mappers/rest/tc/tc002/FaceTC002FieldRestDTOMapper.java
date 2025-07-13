package org.twins.face.mappers.rest.tc.tc002;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dto.rest.tc.tc002.FaceTC002FieldDTOv1;

@Component
@RequiredArgsConstructor
public class FaceTC002FieldRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldEntity, FaceTC002FieldDTOv1> {
    private final I18nService i18nService;

    @MapperModePointerBinding(modes = FaceTC002Modes.FaceTC0022TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @Override
    public void map(TwinClassFieldEntity src, FaceTC002FieldDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setLabel(i18nService.translateToLocale(src.getNameI18nId()))
                .setTwinClassFieldId(src.getId());
        if (mapperContext.hasModeButNot(FaceTC002Modes.FaceTC0022TwinClassFieldMode.HIDE)) {
            twinClassFieldRestDTOMapper.postpone(src, mapperContext.forkOnPoint(FaceTC002Modes.FaceTC0022TwinClassFieldMode.SHORT));
        }
    }
}
