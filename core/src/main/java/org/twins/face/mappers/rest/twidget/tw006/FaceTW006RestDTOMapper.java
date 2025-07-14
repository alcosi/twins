package org.twins.face.mappers.rest.twidget.tw006;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceTwidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dao.twidget.tw006.FaceTW006ActionEntity;
import org.twins.face.dao.twidget.tw006.FaceTW006ActionRepository;
import org.twins.face.dao.twidget.tw006.FaceTW006Entity;
import org.twins.face.dto.rest.twidget.tw006.FaceTW006ActionDTOv1;
import org.twins.face.dto.rest.twidget.tw006.FaceTW006DTOv1;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FaceTW006RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceTW006Entity>, FaceTW006DTOv1> {

    private final FaceTwidgetRestDTOMapper faceTwidgetRestDTOMapper;
    private final FaceTW006ActionRepository faceTW006ActionRepository;
    private final I18nService i18nService;

    @Override
    public void map(PointedFace<FaceTW006Entity> src, FaceTW006DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceTwidgetRestDTOMapper.map(src, dst, mapperContext);

        List<FaceTW006ActionEntity> actionEntities = faceTW006ActionRepository.findByFaceTW006Id(src.getConfig().getId());

        dst.setActions(
                actionEntities.stream()
                        .map(this::mapTWActionEntityToDTO)
                        .toList()
        );
    }

    private FaceTW006ActionDTOv1 mapTWActionEntityToDTO(FaceTW006ActionEntity actionEntity) {
        FaceTW006ActionDTOv1 ret = new FaceTW006ActionDTOv1();

        ret
                .setActionId(actionEntity.getTwinActionId())
                .setFaceTW006Id(actionEntity.getFaceTW006Id())
                .setLabel(
                        i18nService.translateToLocale(
                                actionEntity.getLabelI18nId() != null
                                        ? actionEntity.getLabelI18nId()
                                        : actionEntity.getTwinActionEntity().getNameI18nId()
                        )
                );

        return ret;
    }
}
