package org.twins.face.mappers.rest.twidget.tw004;

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
import org.twins.face.dao.twidget.tw004.FaceTW004Entity;
import org.twins.face.dto.rest.twidget.tw004.FaceTW004DTOv1;


@Component
@RequiredArgsConstructor
public class FaceTW004RestDTOMapper extends RestSimpleDTOMapper<TwidgetConfig<FaceTW004Entity>, FaceTW004DTOv1> {
    protected final FaceTwidgetRestDTOMapper faceTwidgetRestDTOMapper;
    private final I18nService i18nService;
    @MapperModePointerBinding(modes = FaceTW004Modes.FaceTW0042TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @Override
    public void map(TwidgetConfig<FaceTW004Entity> src, FaceTW004DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceTwidgetRestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setKey(src.getConfig().getKey());
            case DETAILED -> dst
                    .setKey(src.getConfig().getKey())
                    .setLabel(i18nService.translateToLocale(src.getConfig().getLabelI18nId() != null ?
                            src.getConfig().getLabelI18nId() : src.getConfig().getTwinClassField().getNameI18nId()))
                    .setTwinClassFieldId(src.getConfig().getTwinClassFieldId());
        }
        if (mapperContext.hasModeButNot(FaceTW004Modes.FaceTW0042TwinClassFieldMode.HIDE)) {
            dst.setTwinClassFieldId(src.getConfig().getTwinClassFieldId());
            twinClassFieldRestDTOMapper.postpone(src.getConfig().getTwinClassField(), mapperContext.forkOnPoint(FaceTW004Modes.FaceTW0042TwinClassFieldMode.SHORT));
        }
    }
}
