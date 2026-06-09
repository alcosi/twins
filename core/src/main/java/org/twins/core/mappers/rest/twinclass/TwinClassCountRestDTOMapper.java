package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.twinclass.TwinClassCountDTOv1;
import org.twins.core.enums.sort.TwinClassGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TwinClassCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinClassEntity, TwinClassGroupField>, TwinClassCountDTOv1> {
    @MapperModePointerBinding(modes = PermissionMode.TwinClass2PermissionMode.class)
    private final PermissionRestDTOMapper permissionRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassFreezeMode.TwinClass2TwinClassFreezeMode.class)
    private final TwinClassFreezeDTOMapper twinClassFreezeDTOMapper;

    @MapperModePointerBinding(modes = {
            TwinClassMode.TwinClassHead2TwinClassMode.class,
            TwinClassMode.TwinClassExtends2TwinClassMode.class,
    })
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.TwinClass2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    @MapperModePointerBinding(modes = FaceMode.TwinClassPage2FaceMode.class)
    private final FaceRestDTOMapper faceRestDTOMapper;

    private final TwinClassService twinClassService;

    @Override
    public void map(CountResult<TwinClassEntity, TwinClassGroupField> src, TwinClassCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setOwnerType(entity.getOwnerType())
                .setAbstractt(entity.getAbstractt())
                .setSegment(entity.getSegment())
                .setTwinClassFreezeId(entity.getTwinClassFreezeId())
                .setHeadTwinClassId(entity.getHeadTwinClassId())
                .setExtendsTwinClassId(entity.getExtendsTwinClassId())
                .setMarkerDataListId(entity.getMarkerDataListId())
                .setTagDataListId(entity.getTagDataListId())
                .setTwinflowSchemaSpace(entity.getTwinflowSchemaSpace())
                .setTwinClassSchemaSpace(entity.getTwinClassSchemaSpace())
                .setAliasSpace(entity.getAliasSpace())
                .setViewPermissionId(entity.getViewPermissionId())
                .setHeadHunterFeaturerId(entity.getHeadHunterFeaturerId())
                .setEditPermissionId(entity.getEditPermissionId())
                .setDeletePermissionId(entity.getDeletePermissionId())
                .setAssigneeRequired(entity.getAssigneeRequired())
                .setUniqueName(entity.getUniqueName())
                .setHasDynamicMarkers(entity.getHasDynamicMarkers())
                .setBreadCrumbsFaceId(entity.getBreadCrumbsFaceId())
                .setPageFaceId(entity.getPageFaceId())
                .setCount(src.getCount());

        if (needLoad(mapperContext, TwinClassMode.TwinClassHead2TwinClassMode.HIDE, src, TwinClassGroupField.headTwinClassId)) {
            twinClassService.loadHeadTwinClass(entity);
            twinClassRestDTOMapper.postpone(entity.getHeadTwinClass(), mapperContext.forkOnPoint(TwinClassMode.TwinClassHead2TwinClassMode.SHORT));
        }
        if (needLoad(mapperContext, TwinClassMode.TwinClassExtends2TwinClassMode.HIDE, src, TwinClassGroupField.extendsTwinClassId)) {
            twinClassService.loadExtendsTwinClass(entity);
            twinClassRestDTOMapper.convertOrPostpone(entity.getExtendsTwinClass(), mapperContext.forkOnPoint(TwinClassMode.TwinClassExtends2TwinClassMode.SHORT));
        }
        if (needLoad(mapperContext, PermissionMode.TwinClass2PermissionMode.HIDE, src, TwinClassGroupField.viewPermissionId, TwinClassGroupField.editPermissionId, TwinClassGroupField.deletePermissionId)) {
            twinClassService.loadPermissions(entity);
            permissionRestDTOMapper.postpone(entity.getViewPermission(), mapperContext.forkOnPoint(PermissionMode.TwinClass2PermissionMode.SHORT));
            permissionRestDTOMapper.postpone(entity.getEditPermission(), mapperContext.forkOnPoint(PermissionMode.TwinClass2PermissionMode.SHORT));
            permissionRestDTOMapper.postpone(entity.getDeletePermission(), mapperContext.forkOnPoint(PermissionMode.TwinClass2PermissionMode.SHORT));
        }
        if (needLoad(mapperContext, TwinClassFreezeMode.TwinClass2TwinClassFreezeMode.HIDE, src, TwinClassGroupField.twinClassFreezeId)) {
            twinClassService.loadFreeze(entity);
            twinClassFreezeDTOMapper.postpone(entity.getTwinClassFreeze(), mapperContext.forkOnPoint(TwinClassFreezeMode.TwinClass2TwinClassFreezeMode.SHORT));
        }
        if (needLoad(mapperContext, FeaturerMode.TwinClass2FeaturerMode.HIDE, src, TwinClassGroupField.headHunterFeaturerId)) {
            featurerRestDTOMapper.postpone(entity.getHeadHunterFeaturerId(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.TwinClass2FeaturerMode.SHORT)));
        }
        if (needLoad(mapperContext, FaceMode.TwinClassPage2FaceMode.HIDE, src, TwinClassGroupField.pageFaceId, TwinClassGroupField.breadCrumbsFaceId)) {
            twinClassService.loadFaces(entity);
            faceRestDTOMapper.postpone(entity.getPageFace(), mapperContext.forkOnPoint(FaceMode.TwinClassPage2FaceMode.SHORT));
            faceRestDTOMapper.postpone(entity.getBreadCrumbsFace(), mapperContext.forkOnPoint(FaceMode.TwinClassPage2FaceMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinClassEntity, TwinClassGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        var entities = srcCollection.stream().map(CountResult::getEntity).toList();
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, TwinClassMode.TwinClassHead2TwinClassMode.HIDE, someCount, TwinClassGroupField.headTwinClassId)) {
            twinClassService.loadHeadTwinClasses(entities);
        }
        if (needLoad(mapperContext, TwinClassMode.TwinClassExtends2TwinClassMode.HIDE, someCount, TwinClassGroupField.extendsTwinClassId)) {
            twinClassService.loadExtendsTwinClasses(entities);
        }
        if (needLoad(mapperContext, DataListMode.TwinClass2MarkerDataListMode.HIDE, someCount, TwinClassGroupField.markerDataListId)) {
            twinClassService.loadMarkerDataList(entities, false);
        }
        if (needLoad(mapperContext, DataListMode.TwinClass2TagDataListMode.HIDE, someCount, TwinClassGroupField.tagDataListId)) {
            twinClassService.loadTagDataList(entities);
        }
        if (needLoad(mapperContext, TwinClassFreezeMode.TwinClass2TwinClassFreezeMode.HIDE, someCount, TwinClassGroupField.twinClassFreezeId)) {
            twinClassService.loadFreeze(entities);
        }
        if (needLoad(mapperContext, PermissionMode.TwinClass2PermissionMode.HIDE, someCount, TwinClassGroupField.viewPermissionId, TwinClassGroupField.deletePermissionId, TwinClassGroupField.editPermissionId)) {
            twinClassService.loadPermissions(entities);
        }
        if (needLoad(mapperContext, FaceMode.TwinClassPage2FaceMode.HIDE, someCount, TwinClassGroupField.pageFaceId, TwinClassGroupField.breadCrumbsFaceId)) {
            twinClassService.loadFaces(entities);
        }
    }
}
