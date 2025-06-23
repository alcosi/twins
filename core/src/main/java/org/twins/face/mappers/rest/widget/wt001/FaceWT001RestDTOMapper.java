package org.twins.face.mappers.rest.widget.wt001;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dao.widget.wt001.FaceWT001Entity;
import org.twins.face.dto.rest.widget.wt001.FaceWT001DTOv1;
import org.twins.face.service.widget.FaceWT001ColumnService;
import org.twins.face.service.widget.FaceWT001Service;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class FaceWT001RestDTOMapper extends RestSimpleDTOMapper<FaceWT001Entity, FaceWT001DTOv1> {
    protected final FaceWT001Service faceWT001Service;
    protected final FaceWT001ColumnService faceWT001ColumnService;
    private final I18nService i18nService;

    @MapperModePointerBinding(modes = FaceWT001Modes.FaceWT001Column2TwinClassFieldMode.class)
    protected final FaceWT001ColumnRestDTOMapper faceWT001ColumnRestDTOMapper;

    @MapperModePointerBinding(modes = FaceMode.ModalFace2FaceMode.class)
    protected final FaceRestDTOMapper faceRestDTOMapper;


    @Override
    public void map(FaceWT001Entity src, FaceWT001DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) { // perhaps we need some separate mode
            case SHORT -> dst
                    .setKey(src.getKey());
            case DETAILED -> {
                faceWT001ColumnService.loadColumns(src);
                dst
                    .setKey(src.getKey())
                    .setLabel(i18nService.translateToLocale(src.getLabelI18nId()))
                    .setTwinClassId(src.getTwinClassId())
                    .setSearchId(src.getSearchId())
                    .setShowCreateButton(src.isShowCreateButton())
                    .setColumns(faceWT001ColumnRestDTOMapper.convertCollection(faceWT001ColumnService.filterVariants(src.getColumns()), mapperContext));}
        }

        if (mapperContext.hasModeButNot(FaceMode.ModalFace2FaceMode.HIDE)) {
            faceRestDTOMapper.postpone(src.getModalFace(), mapperContext.forkOnPoint(FaceMode.ModalFace2FaceMode.SHORT));
            dst.setModalFaceId(src.getModalFaceId());
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FaceWT001Entity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceWT001ColumnService.loadColumns(srcCollection);
    }
}
