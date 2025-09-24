package org.twins.face.mappers.rest.twidget.tw006;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dao.twidget.tw006.FaceTW006ActionEntity;
import org.twins.face.dto.rest.twidget.tw006.FaceTW006ActionDTOv1;

@Component
@RequiredArgsConstructor
public class FaceTW006ActionRestDTOMapper extends RestSimpleDTOMapper<FaceTW006ActionEntity, FaceTW006ActionDTOv1> {

    private final I18nService i18nService;

    @Override
    public void map(FaceTW006ActionEntity src, FaceTW006ActionDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setActionId(src.getTwinActionId())
                .setFaceTW006Id(src.getFaceTW006Id())
                .setLabel(
                        i18nService.translateToLocale(
                                src.getLabelI18nId() != null
                                        ? src.getLabelI18nId()
                                        : src.getTwinActionEntity().getNameI18nId()
                        )
                );
    }
}
