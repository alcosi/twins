package org.twins.face.mappers.rest.twidget.tw004;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceTwidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.face.dao.twidget.tw004.FaceTW004Entity;
import org.twins.face.domain.twidget.tw004.FaceTW004TwinClassField;
import org.twins.face.dto.rest.twidget.tw004.FaceTW004DTOv1;
import org.twins.face.service.twidget.FaceTW004Service;

import java.util.List;

@Deprecated
@Component
@RequiredArgsConstructor
public class FaceTW004RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceTW004Entity>, FaceTW004DTOv1> {
    protected final FaceTwidgetRestDTOMapper faceTwidgetRestDTOMapper;
    private final FaceTW004Service faceTW004Service;
    @MapperModePointerBinding(modes = FaceTW004Modes.FaceTW0042TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @Override
    public void map(PointedFace<FaceTW004Entity> src, FaceTW004DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceTwidgetRestDTOMapper.map(src, dst, mapperContext);
        List<FaceTW004TwinClassField> fields = faceTW004Service.loadFields(src);
        if (CollectionUtils.isEmpty(fields)) {
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "no field configured for twidget");
        }
        FaceTW004TwinClassField faceTW004TwinClassField = fields.getFirst();
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setKey(faceTW004TwinClassField.getField().getKey())
                    .setEditable(true);
            case DETAILED -> dst
                    .setKey(faceTW004TwinClassField.getField().getKey())
                    .setLabel(I18nCacheHolder.addId(faceTW004TwinClassField.getField().getNameI18nId()))
                    .setTwinClassFieldId(faceTW004TwinClassField.getField().getId())
                    .setEditable(faceTW004TwinClassField.isEditable());
        }
        if (mapperContext.hasModeButNot(FaceTW004Modes.FaceTW0042TwinClassFieldMode.HIDE)) {
            dst.setTwinClassFieldId(faceTW004TwinClassField.getField().getId());
            twinClassFieldRestDTOMapper.postpone(faceTW004TwinClassField.getField(), mapperContext.forkOnPoint(FaceTW004Modes.FaceTW0042TwinClassFieldMode.SHORT));
        }
    }
}
