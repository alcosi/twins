package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassBaseDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.service.i18n.I18nService;
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
                if (!permissionService.currentUserHasPermission(Permissions.TWIN_CLASS_MANAGE))
                    throw new ServiceException(ErrorCodeTwins.SHOW_MODE_ACCESS_DENIED, "Show Mode[" + TwinClassMode.MANAGED + "] is not allowed for current user");
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setHeadClassId(src.getHeadTwinClassId())
                        .setAbstractClass(src.isAbstractt())
                        .setMarkersDataListId(src.getMarkerDataListId())
                        .setTagsDataListId(src.getTagDataListId())
                        .setName(i18nService.translateToLocale(src.getNameI18NId()))
                        .setDescription(src.getDescriptionI18NId() != null ? i18nService.translateToLocale(src.getDescriptionI18NId()) : "")
                        .setLogo(src.getLogo())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setHeadHunterFeaturerId(src.getHeadHunterFeaturerId())
                        .setHeadHunterParams(src.getHeadHunterParams())
                        .setViewPermissionId(src.getViewPermissionId())
                        .setCreatePermissionId(src.getCreatePermissionId())
                        .setEditPermissionId(src.getEditPermissionId())
                        .setDeletePermissionId(src.getDeletePermissionId())
                        .setNameI18nId(src.getNameI18NId())
                        .setDescriptionI18nId(src.getDescriptionI18NId())
                        .setExtendsClassId(src.getExtendsTwinClassId())
                        .setTwinflowSchemaSpace(src.isTwinflowSchemaSpace())
                        .setTwinClassSchemaSpace(src.isTwinClassSchemaSpace())
                        .setPermissionSchemaSpace(src.isPermissionSchemaSpace())
                        .setAliasSpace(src.isAliasSpace())
                        .setOwnerType(src.getOwnerType())
                        .setPageFaceId(src.getPageFaceId());
                break;
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setHeadClassId(src.getHeadTwinClassId())
//                        .setHeadClass(convertOrPostpone(src))
                        .setAbstractClass(src.isAbstractt())
                        .setMarkersDataListId(src.getMarkerDataListId())
                        .setTagsDataListId(src.getTagDataListId())
                        .setName(i18nService.translateToLocale(src.getNameI18NId()))
                        .setDescription(src.getDescriptionI18NId() != null ? i18nService.translateToLocale(src.getDescriptionI18NId()) : "")
                        .setLogo(src.getLogo())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setPageFaceId(src.getPageFaceId());
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
        return mapperContext.hasModeOrEmpty(TwinClassMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinClassEntity src) {
        return src.getId().toString();
    }


}
