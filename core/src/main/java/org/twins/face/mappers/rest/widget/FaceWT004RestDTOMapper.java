package org.twins.face.mappers.rest.widget;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dao.widget.FaceWT004Entity;
import org.twins.face.dto.rest.widget.FaceWT004DTOv1;


@Component
@RequiredArgsConstructor
public class FaceWT004RestDTOMapper extends RestSimpleDTOMapper<FaceWT004Entity, FaceWT004DTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    private final I18nService i18nService;


    @Override
    public void map(FaceWT004Entity src, FaceWT004DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setKey(src.getKey());
            case DETAILED -> dst
                    .setKey(src.getKey())
                    .setLabel(i18nService.translateToLocale(src.getLabelI18nId() != null ?
                            src.getLabelI18nId() : src.getTwinClassField().getNameI18nId()))
                    .setI18nTwinClassFieldId(src.getI18nTwinClassFieldId());
        }
    }
}
