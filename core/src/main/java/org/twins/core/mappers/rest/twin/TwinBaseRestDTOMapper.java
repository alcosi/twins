package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.twin.TwinBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.face.FaceService;
import org.twins.core.service.twin.TwinAliasService;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinMode.class, RelationTwinMode.TwinByHeadMode.class})
public class TwinBaseRestDTOMapper extends RestSimpleDTOMapper<TwinEntity, TwinBaseDTOv1> {
    @MapperModePointerBinding(modes = {UserMode.Twin2UserMode.class})
    final UserRestDTOMapper userDTOMapper;

    @MapperModePointerBinding(modes = {StatusMode.Twin2StatusMode.class})
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @MapperModePointerBinding(modes = {TwinAliasMode.class})
    private final TwinAliasRestDTOMapper twinAliasRestDTOMapper;

    @MapperModePointerBinding(modes = {FaceMode.Twin2FaceMode.class})
    private final FaceRestDTOMapper faceRestDTOMapper;

    @Lazy
    @Autowired
    @MapperModePointerBinding(modes = {TwinClassMode.Twin2TwinClassMode.class})
    private TwinClassRestDTOMapper twinClassRestDTOMapper;

    private final FaceService faceService;
    private final TwinService twinService;
    private final TwinAliasService twinAliasService;

    @Override
    public void map(TwinEntity src, TwinBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinMode.SHORT)) {
            case DETAILED:
                dst
                        .id(src.getId())
                        .name(src.getName())
                        .externalId(src.getExternalId())
                        .headTwinId(src.getHeadTwinId())
                        .assignerUserId(src.getAssignerUserId())
                        .authorUserId(src.getCreatedByUserId())
                        .statusId(twinService.getStatusOrFreeze(src).getId())
                        .twinClassId(src.getTwinClassId())
                        .description(src.getDescription())
                        .ownerBusinessAccountId(src.getOwnerBusinessAccountId())
                        .ownerUserId(src.getOwnerUserId())
                        .createdAt(src.getCreatedAt().toLocalDateTime())
                        .pageFaceId(faceService.resolvePageFaceId(src))
                        .breadCrumbsFaceId(faceService.resolveBreadCrumbsFaceId(src))
                        .freeze(twinService.checkIsFreezeStatus(src));
                break;
            case SHORT:
                dst
                        .id(src.getId())
                        .name(src.getName())
                        .freeze(twinService.checkIsFreezeStatus(src));
                break;
        }
        if (mapperContext.hasModeButNot(StatusMode.Twin2StatusMode.HIDE)) {
            TwinStatusEntity statusEntity = twinService.getStatusOrFreeze(src);
            dst.statusId(statusEntity.getId());
            twinStatusRestDTOMapper.postpone(statusEntity, mapperContext.forkOnPoint(StatusMode.Twin2StatusMode.SHORT));

        }
        if (mapperContext.hasModeButNot(UserMode.Twin2UserMode.HIDE)) {
            dst
                    .assignerUserId(src.getAssignerUserId())
                    .authorUserId(src.getCreatedByUserId());
            userDTOMapper.convertOrPostpone(src.getAssignerUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Twin2UserMode.SHORT)));
            userDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Twin2UserMode.SHORT)));

        }
        if (mapperContext.hasModeButNot(TwinClassMode.Twin2TwinClassMode.HIDE)) {
            dst.twinClassId(src.getTwinClassId());
            twinClassRestDTOMapper.convertOrPostpone(src.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.Twin2TwinClassMode.SHORT)); //todo deep recursion risk
        }

        if (mapperContext.hasModeButNot(RelationTwinMode.TwinByHeadMode.WHITE)) {
            twinService.loadHeadForTwin(src);
            dst.headTwinId(src.getHeadTwinId());
            this.convertOrPostpone(src.getHeadTwin(), mapperContext.forkOnPoint(RelationTwinMode.TwinByHeadMode.GREEN));  //head twin will be much less detail
        }
        if (mapperContext.hasModeButNot(TwinAliasMode.HIDE)) {
            twinAliasService.loadAliases(src);
            dst.aliases(twinAliasRestDTOMapper.convert(src, mapperContext));
        }
        if (mapperContext.hasModeButNot(FaceMode.Twin2FaceMode.HIDE)) {
            faceService.loadFaces(src);

            faceRestDTOMapper.postpone(
                    src.getPageFace(),
                    mapperContext.forkOnPoint(FaceMode.Twin2FaceMode.SHORT)
            );
            faceRestDTOMapper.postpone(
                    src.getBreadCrumbsFace(),
                    mapperContext.forkOnPoint(FaceMode.Twin2FaceMode.SHORT)
            );

            dst
                    .breadCrumbsFaceId(faceService.resolveBreadCrumbsFaceId(src))
                    .pageFaceId(faceService.resolvePageFaceId(src));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinEntity src) {
        return src.getId().toString();
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinAliasMode.HIDE))
            twinAliasService.loadAliases(srcCollection);
        if (mapperContext.hasModeButNot(RelationTwinMode.TwinByHeadMode.WHITE)) {
            twinService.loadHeadForTwin(srcCollection);
        }
    }

}
