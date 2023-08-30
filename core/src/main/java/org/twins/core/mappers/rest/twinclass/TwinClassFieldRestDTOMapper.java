package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;
import org.twins.core.featurer.fieldtyper.FieldTypeUIDescriptor;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.mappers.rest.RestListDTOMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinClassFieldRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldEntity, TwinClassFieldDTOv1> {
    final I18nService i18nService;
    final FeaturerService featurerService;

    @Override
    public void map(TwinClassFieldEntity src, TwinClassFieldDTOv1 dst) throws ServiceException {
        FieldTyper fieldTyper = featurerService.getFeaturer(src.getFieldTyperFeaturer(), FieldTyper.class);
        FieldTypeUIDescriptor fieldTypeUIDescriptor = fieldTyper.getUiDescriptor(src.getFieldTyperParams());
        dst
                .id(src.getId())
                .key(src.getKey())
                .name(i18nService.translateToLocale(src.getNameI18n()))
                .required(src.isRequired())
                .description(src.getDescriptionI18n() != null ? i18nService.translateToLocale(src.getDescriptionI18n()) : "")
                .type(fieldTypeUIDescriptor.type())
                .typeParams(fieldTypeUIDescriptor.params())
        ;
    }
}
