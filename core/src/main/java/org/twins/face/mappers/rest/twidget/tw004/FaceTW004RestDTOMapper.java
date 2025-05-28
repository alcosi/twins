package org.twins.face.mappers.rest.twidget.tw004;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.face.TwidgetConfig;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceTwidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dao.twidget.tw004.FaceTW004Entity;
import org.twins.face.dto.rest.twidget.tw004.FaceTW004DTOv1;
import org.twins.face.service.twidget.FaceTW004Service;

import java.util.List;

@Deprecated
@Component
@RequiredArgsConstructor
public class FaceTW004RestDTOMapper extends RestSimpleDTOMapper<TwidgetConfig<FaceTW004Entity>, FaceTW004DTOv1> {
    protected final FaceTwidgetRestDTOMapper faceTwidgetRestDTOMapper;
    private final I18nService i18nService;
    private final FaceTW004Service faceTW004Service;
    @MapperModePointerBinding(modes = FaceTW004Modes.FaceTW0042TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @Override
    public void map(TwidgetConfig<FaceTW004Entity> src, FaceTW004DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceTwidgetRestDTOMapper.map(src, dst, mapperContext);
        List<TwinClassFieldEntity> fields = faceTW004Service.loadFields(src.getTargetTwin().getTwinClassId(), src.getConfig());
        if (CollectionUtils.isEmpty(fields)) {
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "no field configured for twidget");
        }
        TwinClassFieldEntity twinClassField = fields.getFirst();
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setKey(twinClassField.getKey())
                    .setEditable(true);
            case DETAILED -> dst
                    .setKey(twinClassField.getKey())
                    .setLabel(i18nService.translateToLocale(twinClassField.getNameI18nId()))
                    .setTwinClassFieldId(twinClassField.getId())
                    .setEditable(true);
        }
        if (mapperContext.hasModeButNot(FaceTW004Modes.FaceTW0042TwinClassFieldMode.HIDE)) {
            dst.setTwinClassFieldId(twinClassField.getId());
            twinClassFieldRestDTOMapper.postpone(twinClassField, mapperContext.forkOnPoint(FaceTW004Modes.FaceTW0042TwinClassFieldMode.SHORT));
        }
    }
}
