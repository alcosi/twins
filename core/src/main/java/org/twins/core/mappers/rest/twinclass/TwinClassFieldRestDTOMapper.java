package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.twins.core.service.i18n.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldMode;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinClassFieldMode.class)
public class TwinClassFieldRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldEntity, TwinClassFieldDTOv1> {

    private final TwinClassFieldDescriptorRestDTOMapper twinClassFieldDescriptorRestDTOMapper;

    private final I18nService i18nService;
    private final FeaturerService featurerService;
    private final PermissionService permissionService;

    @Override
    public void map(TwinClassFieldEntity src, TwinClassFieldDTOv1 dst, MapperContext mapperContext) throws Exception {
        FieldTyper fieldTyper;
        FieldDescriptor fieldDescriptor;
        switch (mapperContext.getModeOrUse(TwinClassFieldMode.DETAILED)) {
            case MANAGED:
                if (!permissionService.currentUserHasPermission(Permissions.TWIN_CLASS_MANAGE))
                    throw new ServiceException(ErrorCodeTwins.SHOW_MODE_ACCESS_DENIED, "Show Mode[" + TwinClassFieldMode.MANAGED + "] is not allowed for current user");
                fieldTyper = featurerService.getFeaturer(src.getFieldTyperFeaturer(), FieldTyper.class);
                fieldDescriptor = fieldTyper.getFieldDescriptor(src);
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setName(i18nService.translateToLocale(src.getNameI18nId()))
                        .setRequired(src.getRequired())
                        .setDescription(src.getDescriptionI18nId() != null ? i18nService.translateToLocale(src.getDescriptionI18nId()) : "")
                        .setTwinClassId(src.getTwinClassId())
                        .setNameI18nId(src.getNameI18nId())
                        .setDescriptionI18nId(src.getDescriptionI18nId())
                        .setFieldTyperFeaturerId(src.getFieldTyperFeaturerId())
                        .setTwinSorterFeaturerId(src.getTwinSorterFeaturerId())
                        .setTwinSorterParams(src.getFieldTyperParams())
                        .setViewPermissionId(src.getViewPermissionId())
                        .setEditPermissionId(src.getEditPermissionId())
                        .setDescriptor(twinClassFieldDescriptorRestDTOMapper.convert(fieldDescriptor, mapperContext))
                        .setExternalId(src.getExternalId());
                break;
            case DETAILED:
                fieldTyper = featurerService.getFeaturer(src.getFieldTyperFeaturer(), FieldTyper.class);
                fieldDescriptor = fieldTyper.getFieldDescriptor(src);
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setName(i18nService.translateToLocale(src.getNameI18nId()))
                        .setRequired(src.getRequired())
                        .setDescription(src.getDescriptionI18nId() != null ? i18nService.translateToLocale(src.getDescriptionI18nId()) : "")
                        .setDescriptor(twinClassFieldDescriptorRestDTOMapper.convert(fieldDescriptor, mapperContext))
                        .setExternalId(src.getExternalId());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinClassFieldMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinClassFieldEntity src) {
        return src.getId().toString();
    }
}
