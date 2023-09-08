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
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestListDTOMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinClassFieldRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldEntity, TwinClassFieldDTOv1> {
    final I18nService i18nService;
    final FeaturerService featurerService;

    @Override
    public void map(TwinClassFieldEntity src, TwinClassFieldDTOv1 dst, MapperProperties mapperProperties) throws ServiceException {
        FieldTyper fieldTyper = featurerService.getFeaturer(src.fieldTyperFeaturer(), FieldTyper.class);
        FieldTypeUIDescriptor fieldTypeUIDescriptor = fieldTyper.getUiDescriptor(src.fieldTyperParams());
        switch (mapperProperties.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dst
                        .name(i18nService.translateToLocale(src.nameI18n()))
                        .required(src.required())
                        .description(src.descriptionI18n() != null ? i18nService.translateToLocale(src.descriptionI18n()) : "")
                        .type(fieldTypeUIDescriptor.type())
                        .typeParams(fieldTypeUIDescriptor.params());
            case ID_KEY_ONLY:
                dst
                        .id(src.id())
                        .key(src.key());

        }
    }

    public enum Mode implements MapperMode {
        ID_KEY_ONLY, DETAILED,
        ;
        public static final String _ID_KEY_ONLY = "ID_KEY_ONLY";
        public static final String _DETAILED = "DETAILED";
        ;
    }
}
