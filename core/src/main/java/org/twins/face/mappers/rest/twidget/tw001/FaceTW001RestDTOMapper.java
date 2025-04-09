package org.twins.face.mappers.rest.twidget.tw001;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.domain.face.TwidgetConfig;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceTwidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dao.twiget.tw001.FaceTW001Entity;
import org.twins.face.dto.rest.twidget.tw001.FaceTW001DTOv1;


@Component
@RequiredArgsConstructor
public class FaceTW001RestDTOMapper extends RestSimpleDTOMapper<TwidgetConfig<FaceTW001Entity>, FaceTW001DTOv1> {
    protected final FaceTwidgetRestDTOMapper faceTwidgetRestDTOMapper;
    private final I18nService i18nService;
    @MapperModePointerBinding(modes = FaceTW001Modes.FaceTW0012TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @Override
    public void map(TwidgetConfig<FaceTW001Entity> src, FaceTW001DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceTwidgetRestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setKey(src.getConfig().getKey());
            case DETAILED -> dst
                    .setKey(src.getConfig().getKey())
                    .setLabel(i18nService.translateToLocale(src.getConfig().getLabelI18nId()))
                    .setImagesTwinClassFieldId(src.getConfig().getImagesTwinClassFieldId());
        }
        if (mapperContext.hasModeButNot(FaceTW001Modes.FaceTW0012TwinClassFieldMode.HIDE)) {
            dst.setImagesTwinClassFieldId(src.getConfig().getImagesTwinClassFieldId());
            twinClassFieldRestDTOMapper.postpone(src.getConfig().getImagesTwinClassField(), mapperContext.forkOnPoint(FaceTW001Modes.FaceTW0012TwinClassFieldMode.SHORT));
        }
    }
}
