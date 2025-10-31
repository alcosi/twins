package org.twins.face.mappers.rest.twidget.tw004;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceTwidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.face.dao.twidget.tw004.FaceTW004Entity;
import org.twins.face.domain.twidget.tw004.FaceTW004TwinClassField;
import org.twins.face.dto.rest.twidget.tw004.FaceTW004DTOv2;
import org.twins.face.service.twidget.FaceTW004Service;

import java.util.List;


@Component
@RequiredArgsConstructor
public class FaceTW004v2RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceTW004Entity>, FaceTW004DTOv2> {
    protected final FaceTwidgetRestDTOMapper faceTwidgetRestDTOMapper;
    private final FaceTW004Service faceTW004Service;
    private final FaceTW004v2FieldRestDTOMapper faceTW004v2FieldRestDTOMapper;

    @Override
    public void map(PointedFace<FaceTW004Entity> src, FaceTW004DTOv2 dst, MapperContext mapperContext) throws Exception {
        faceTwidgetRestDTOMapper.map(src, dst, mapperContext);
        List<FaceTW004TwinClassField> fields = faceTW004Service.loadFields(src);

        dst
                .setLabel(I18nCacheHolder.addId(src.getConfig().getLabelI18nId()))
                .setStyleClasses(src.getConfig().getStyleClasses())
                .setFields(faceTW004v2FieldRestDTOMapper.convertCollection(fields));
    }
}
