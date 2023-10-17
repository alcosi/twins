package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.FeaturerService;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinClassFieldRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldEntity, TwinClassFieldDTOv1> {
    final I18nService i18nService;
    final FeaturerService featurerService;
    final TwinClassFieldDescriptorRestDTOMapper twinClassFieldDescriptorRestDTOMapper;

    @Override
    public void map(TwinClassFieldEntity src, TwinClassFieldDTOv1 dst, MapperContext mapperContext) throws Exception {
        FieldTyper fieldTyper = featurerService.getFeaturer(src.getFieldTyperFeaturer(), FieldTyper.class);
        FieldDescriptor fieldDescriptor = fieldTyper.getFieldDescriptor(src);
        switch (mapperContext.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dst
                        .name(i18nService.translateToLocale(src.getNameI18n()))
                        .required(src.isRequired())
                        .description(src.getDescriptionI18n() != null ? i18nService.translateToLocale(src.getDescriptionI18n()) : "")
                        .descriptor(twinClassFieldDescriptorRestDTOMapper.convert(fieldDescriptor));
            case ID_KEY_ONLY:
                dst
                        .id(src.getId())
                        .key(src.getKey());

        }
    }

    public enum Mode implements MapperMode {
        ID_KEY_ONLY, DETAILED;

        public static final String _ID_KEY_ONLY = "ID_KEY_ONLY";
        public static final String _DETAILED = "DETAILED";
        ;
    }

    @Override
    public String getObjectCacheId(TwinClassFieldEntity src) {
        return src.getId().toString();
    }
}
