package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassBaseDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassExternalJsonMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.resource.ResourceService;

@Slf4j
@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinClassMode.class, TwinClassExternalJsonMode.class})
public class TwinClassBaseRestDTOMapper extends RestSimpleDTOMapper<TwinClassEntity, TwinClassBaseDTOv1> {

    private final I18nService i18nService;
    private final PermissionService permissionService;
    private final ResourceService resourceService;

    @Override
    public void map(TwinClassEntity src, TwinClassBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        if (mapperContext.hasMode(TwinClassMode.MANAGED) && !permissionService.currentUserHasPermission(Permissions.TWIN_CLASS_MANAGE)) {
            log.warn("Show Mode [{}] is not allowed for current user", TwinClassMode.MANAGED);
            mapperContext.setMode(TwinClassMode.DETAILED);
        }

        switch (mapperContext.getModeOrUse(TwinClassMode.DETAILED)) {
            case MANAGED:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setHeadClassId(src.getHeadTwinClassId())
                        .setAbstractClass(src.getAbstractt())
                        .setMarkersDataListId(src.getMarkerDataListId())
                        .setTagsDataListId(src.getTagDataListId())
                        .setTwinClassFreezeId(src.getTwinClassFreezeId())
                        .setName(I18nCacheHolder.addId(src.getNameI18NId()))
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18NId()))
                        .setIconDark(resourceService.getResourceUri(src.getIconDarkResource()))
                        .setIconLight(resourceService.getResourceUri(src.getIconLightResource()))
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
                        .setTwinflowSchemaSpace(src.getTwinflowSchemaSpace())
                        .setTwinClassSchemaSpace(src.getTwinClassSchemaSpace())
                        .setPermissionSchemaSpace(src.getPermissionSchemaSpace())
                        .setAliasSpace(src.getAliasSpace())
                        .setOwnerType(src.getOwnerType())
                        .setPageFaceId(src.getPageFaceId())
                        .setBreadCrumbsFaceId(src.getBreadCrumbsFaceId())
                        .setInheritedPageFaceId(src.getInheritedPageFaceId())
                        .setInheritedBreadCrumbsFaceId(src.getInheritedBreadCrumbsFaceId())
                        .setAssigneeRequired(src.getAssigneeRequired())
                        .setExternalId(src.getExternalId())
                        .setExternalProperties(src.getExternalProperties())
                        .setSegment(src.getSegment())
                        .setHasSegment(src.getHasSegment());
                break;
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setHeadClassId(src.getHeadTwinClassId())
//                        .setHeadClass(convertOrPostpone(src))
                        .setAbstractClass(src.getAbstractt())
                        .setMarkersDataListId(src.getMarkerDataListId())
                        .setTagsDataListId(src.getTagDataListId())
                        .setTwinClassFreezeId(src.getTwinClassFreezeId())
                        .setName(I18nCacheHolder.addId(src.getNameI18NId()))
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18NId()))
                        .setIconDark(resourceService.getResourceUri(src.getIconDarkResource()))
                        .setIconLight(resourceService.getResourceUri(src.getIconLightResource()))
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setExternalId(src.getExternalId())
                        .setExternalProperties(src.getExternalProperties())
                        .setSegment(src.getSegment())
                        .setHasSegment(src.getHasSegment());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey());
                break;
        }
        if (mapperContext.hasModeButNot(TwinClassExternalJsonMode.HIDE))
            dst.setExternalJson(src.getExternalJson());
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
