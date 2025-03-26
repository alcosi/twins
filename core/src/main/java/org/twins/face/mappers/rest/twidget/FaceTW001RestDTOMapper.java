package org.twins.face.mappers.rest.twidget;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dao.twiget.FaceTW001Entity;
import org.twins.face.dto.rest.twidget.FaceTW001DTOv1;


@Component
@RequiredArgsConstructor
public class FaceTW001RestDTOMapper extends RestSimpleDTOMapper<FaceTW001Entity, FaceTW001DTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    private final I18nService i18nService;


    @Override
    public void map(FaceTW001Entity src, FaceTW001DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setKey(src.getKey());
            case DETAILED -> dst
                    .setKey(src.getKey())
                    .setLabel(i18nService.translateToLocale(src.getLabelI18nId()))
                    .setImagesTwinClassFieldId(src.getImagesTwinClassFieldId())
                    .setImagesTwinClassFieldKey(src.getImagesTwinClassField().getKey());
        }
    }
}
