package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dto.rest.domain.DomainViewDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.permission.PermissionSchemaRestDTOMapper;
import org.twins.core.mappers.rest.tier.TierRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twinclass.TwinClassSchemaDTOMapper;
import org.twins.core.service.domain.DomainService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainMode.class)
public class DomainViewRestDTOMapper extends RestSimpleDTOMapper<DomainEntity, DomainViewDTOv1> {
    protected final DomainService domainService;
    protected final DomainViewPublicRestDTOMapper domainViewPublicRestDTOMapper;

    @MapperModePointerBinding(modes = FaceMode.DomainNavbar2FaceMode.class)
    protected final FaceRestDTOMapper faceRestDTOMapper;
    @MapperModePointerBinding(modes = {
            FeaturerMode.DomainUserGroupManager2FeaturerMode.class})
    private final FeaturerRestDTOMapper featurerRestDTOMapper;
    @MapperModePointerBinding(modes = {
            TwinMode.DomainBusinessAccountTemplate2TwinMode.class,
            TwinMode.DomainUserTemplate2TwinMode.class})
    private final TwinRestDTOMapperV2 twinRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionSchemaMode.Domain2PermissionSchemaMode.class)
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassSchemaMode.Domain2TwinClassSchemaMode.class)
    private final TwinClassSchemaDTOMapper twinclassSchemaDTOMapper;

    @MapperModePointerBinding(modes = TierMode.Domain2TierMode.class)
    private final TierRestDTOMapper tierRestDTOMapper;

    @Override
    public void map(DomainEntity src, DomainViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        domainViewPublicRestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(DomainMode.DETAILED)) {
            case DETAILED ->
                dst
                        .setType(src.getDomainType())
                        .setBusinessAccountInitiatorFeaturerId(src.getBusinessAccountInitiatorFeaturerId())
                        .setBusinessAccountInitiatorParams(src.getBusinessAccountInitiatorParams())
                        .setUserGroupManagerFeaturerId(src.getUserGroupManagerFeaturerId())
                        .setUserGroupManagerParams(src.getUserGroupManagerParams())
                        .setDefaultLocale(src.getDefaultI18nLocaleId() != null ? src.getDefaultI18nLocaleId().getLanguage() : null)
                        .setTwinflowSchemaId(src.getTwinflowSchemaId())
                        .setPermissionSchemaId(src.getPermissionSchemaId())
                        .setTwinClassSchemaId(src.getTwinClassSchemaId())
                        .setBusinessAccountTemplateTwinId(src.getBusinessAccountTemplateTwinId())
                        .setAncestorTwinClassId(src.getAncestorTwinClassId())
                        .setDefaultTierId(src.getDefaultTierId())
                        .setAttachmentStorageUsedCount(src.getAttachmentsStorageUsedCount())
                        .setAttachmentStorageUsedSize(src.getAttachmentsStorageUsedSize())
                        .setDomainUserTemplateTwinId(src.getDomainUserTemplateTwinId())
                        .setResourceStorageId(src.getResourcesStorageId())
                        .setAttachmentStorageId(src.getAttachmentsStorageId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setName(src.getName());
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setKey(src.getKey());
        }
        if (mapperContext.hasModeButNot(FaceMode.DomainNavbar2FaceMode.HIDE)) {
            faceRestDTOMapper.postpone(src.getNavbarFace(), mapperContext.forkOnPoint(FaceMode.DomainNavbar2FaceMode.SHORT));
            dst.setNavbarFaceId(src.getNavbarFaceId());
        }
        if (mapperContext.hasModeButNot(FeaturerMode.DomainUserGroupManager2FeaturerMode.HIDE)) {
            dst.setUserGroupManagerFeaturerId(src.getUserGroupManagerFeaturerId());
            domainService.loadUserGroupManager(src);
            featurerRestDTOMapper.postpone(src.getUserGroupManagerFeaturer(), mapperContext.forkOnPoint(FeaturerMode.DomainUserGroupManager2FeaturerMode.SHORT));
        }
        if (mapperContext.hasModeButNot(PermissionSchemaMode.Domain2PermissionSchemaMode.HIDE)) {
            dst.setPermissionSchemaId(src.getPermissionSchemaId());
            permissionSchemaRestDTOMapper.postpone(src.getPermissionSchema(), mapperContext.forkOnPoint(PermissionSchemaMode.Domain2PermissionSchemaMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinClassSchemaMode.Domain2TwinClassSchemaMode.HIDE)) {
            dst.setTwinClassSchemaId(src.getTwinClassSchemaId());
            twinclassSchemaDTOMapper.postpone(src.getTwinClassSchema(), mapperContext.forkOnPoint(TwinClassSchemaMode.Domain2TwinClassSchemaMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinMode.DomainBusinessAccountTemplate2TwinMode.HIDE)) {
            dst.setBusinessAccountTemplateTwinId(src.getBusinessAccountTemplateTwinId());
            twinRestDTOMapper.postpone(src.getBusinessAccountTemplateTwin(), mapperContext.forkOnPoint(TwinMode.DomainBusinessAccountTemplate2TwinMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TierMode.Domain2TierMode.HIDE)) {
            dst.setDefaultTierId(src.getDefaultTierId());
            tierRestDTOMapper.postpone(src.getDefaultTier(), mapperContext.forkOnPoint(TierMode.Domain2TierMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinMode.DomainUserTemplate2TwinMode.HIDE)) {
            dst.setDomainUserTemplateTwinId(src.getDomainUserTemplateTwinId());
            twinRestDTOMapper.postpone(src.getDomainUserTemplateTwin(), mapperContext.forkOnPoint(TwinMode.DomainUserTemplate2TwinMode.SHORT));
        }

    }

    @Override
    public void beforeCollectionConversion(Collection<DomainEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(FeaturerMode.DomainUserGroupManager2FeaturerMode.HIDE)) {
            domainService.loadUserGroupManagers(srcCollection);
        }
    }
}
