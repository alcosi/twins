package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassBaseDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinClassMode.class)
public class TwinClassBaseRestDTOMapper extends RestSimpleDTOMapper<TwinClassEntity, TwinClassBaseDTOv1> {
    private final I18nService i18nService;
    private final PermissionService permissionService;

    @Override
    public void map(TwinClassEntity src, TwinClassBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinClassMode.DETAILED)) {
            case MANAGED:
                if(permissionService.currentUserHasPermission(Permissions.TWIN_CLASS_MANAGE.getId())) {
                    dst
                            .id(src.getId())
                            .key(src.getKey())
                            .headClassId(src.getHeadTwinClassId())
                            .abstractClass(src.isAbstractt())
                            .markersDataListId(src.getMarkerDataListId())
                            .tagsDataListId(src.getTagDataListId())
                            .name(i18nService.translateToLocale(src.getNameI18NId()))
                            .description(src.getDescriptionI18NId() != null ? i18nService.translateToLocale(src.getDescriptionI18NId()) : "")
                            .logo(src.getLogo())
                            .createdAt(src.getCreatedAt().toLocalDateTime())
                            .headHunterFeaturerId(src.getHeadHunterFeaturerId())
                            .headHunterParams(src.getHeadHunterParams())
                            .viewPermissionId(src.getViewPermissionId())
                            .nameI18nId(src.getNameI18NId())
                            .descriptionI18nId(src.getDescriptionI18NId())
                            .extendsClassId(src.getExtendsTwinClassId())
                            .twinflowSchemaSpace(src.isTwinflowSchemaSpace())
                            .twinClassSchemaSpace(src.isTwinClassSchemaSpace())
                            .permissionSchemaSpace(src.isPermissionSchemaSpace())
                            .aliasSpace(src.isAliasSpace())
                            .ownerType(src.getOwnerType());
                } else {
                    throw new ServiceException(ErrorCodeTwins.SHOW_MODE_ACCESS_DENIED, "Show Mode[" + TwinClassMode.MANAGED + "] is not allowed for current user");
                }
                break;
            case DETAILED:
                dst
                        .id(src.getId())
                        .key(src.getKey())
                        .headClassId(src.getHeadTwinClassId())
//                        .headClass(convertOrPostpone(src))
                        .abstractClass(src.isAbstractt())
                        .markersDataListId(src.getMarkerDataListId())
                        .tagsDataListId(src.getTagDataListId())
                        .name(i18nService.translateToLocale(src.getNameI18NId()))
                        .description(src.getDescriptionI18NId() != null ? i18nService.translateToLocale(src.getDescriptionI18NId()) : "")
                        .logo(src.getLogo())
                        .createdAt(src.getCreatedAt().toLocalDateTime());
                break;
            case SHORT:
                dst
                        .id(src.getId())
                        .key(src.getKey());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinClassMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinClassEntity src) {
        return src.getId().toString();
    }


}
