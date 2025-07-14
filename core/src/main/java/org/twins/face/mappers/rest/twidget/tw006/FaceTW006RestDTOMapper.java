package org.twins.face.mappers.rest.twidget.tw006;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceTwidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.face.dao.twidget.tw006.FaceTW006ActionEntity;
import org.twins.face.dao.twidget.tw006.FaceTW006Entity;
import org.twins.face.dto.rest.twidget.tw006.FaceTW006DTOv1;
import org.twins.face.service.twidget.FaceTW006ActionService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FaceTW006RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceTW006Entity>, FaceTW006DTOv1> {

    private final FaceTwidgetRestDTOMapper faceTwidgetRestDTOMapper;
    private final FaceTW006ActionRestDTOMapper faceTW006ActionRestDTOMapper;
    private final FaceTW006ActionService faceTW006ActionService;

    @Override
    public void map(PointedFace<FaceTW006Entity> src, FaceTW006DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceTwidgetRestDTOMapper.map(src, dst, mapperContext);

        List<FaceTW006ActionEntity> actionEntities = faceTW006ActionService.findActionEntitiesByFaceTW006Id(src.getConfig().getId());

        dst.setActions(faceTW006ActionRestDTOMapper.convertCollection(actionEntities));
    }
}
