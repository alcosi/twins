package org.twins.face.mappers.rest.tc.tc001;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dto.rest.tc.tc001.FaceTC001FieldDTOV1;

@Component
@RequiredArgsConstructor
public class FaceTC001FieldRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldEntity, FaceTC001FieldDTOV1> {
    private final I18nService i18nService;

    @MapperModePointerBinding(modes = FaceTC001Modes.FaceTC0012TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @Override
    public void map(TwinClassFieldEntity src, FaceTC001FieldDTOV1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setLabel(i18nService.translateToLocale(src.getNameI18nId()))
                .setTwinClassFieldId(src.getId());
        if (mapperContext.hasModeButNot(FaceTC001Modes.FaceTC0012TwinClassFieldMode.HIDE)){
            twinClassFieldRestDTOMapper.postpone(src, mapperContext.forkOnPoint(FaceTC001Modes.FaceTC0012TwinClassFieldMode.SHORT));
        }
    }
}
