package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.mappers.rest.RestDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinClassFieldRestDTOMapper implements RestDTOMapper<TwinClassFieldEntity, TwinClassFieldDTOv1> {
    final I18nService i18nService;
    final FeaturerService featurerService;

    public TwinClassFieldDTOv1 convert(TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        TwinClassFieldDTOv1 twinClassFieldDTOv1 = new TwinClassFieldDTOv1();
        map(twinClassFieldEntity, twinClassFieldDTOv1);
        return twinClassFieldDTOv1;
    }

    @Override
    public void map(TwinClassFieldEntity src, TwinClassFieldDTOv1 dst) throws ServiceException {
        FieldTyper fieldTyper = featurerService.getFeaturer(src.getFieldTyperFeaturer(), FieldTyper.class);
        dst
                .id(src.getId())
                .key(src.getKey())
                .name(i18nService.translateToLocale(src.getNameI18n()))
                .required(src.isRequired())
                .description(src.getDescriptionI18n() != null ? i18nService.translateToLocale(src.getDescriptionI18n()) : "")
                .type(fieldTyper.getType())
                .typeParams(fieldTyper.getUiParamList(src.getFieldTyperParams()))
        ;
    }
}
