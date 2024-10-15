package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinBaseDTOv2;
import org.twins.core.mappers.rest.mappercontext.*;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.twin.TwinAliasService;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = { RelationTwinMode.TwinByHeadMode.class })
public class TwinBaseV2RestDTOMapper extends RestSimpleDTOMapper<TwinEntity, TwinBaseDTOv2> {

    private final TwinBaseRestDTOMapper twinBaseRestDTOMapper;

    @MapperModePointerBinding(modes = {UserMode.Twin2UserMode.class})
    final UserRestDTOMapper userDTOMapper;

    @MapperModePointerBinding(modes = {StatusMode.Twin2StatusMode.class})
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @MapperModePointerBinding(modes = {TwinAliasMode.class})
    private final TwinAliasRestDTOMapper twinAliasRestDTOMapper;

    private final TwinService twinService;
    private final TwinAliasService twinAliasService;

    @Lazy
    @Autowired
    @MapperModePointerBinding(modes = {TwinClassMode.Twin2TwinClassMode.class})
    private TwinClassRestDTOMapper twinClassRestDTOMapper;

    @Override
    public void map(TwinEntity src, TwinBaseDTOv2 dst, MapperContext mapperContext) throws Exception {
        twinBaseRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(StatusMode.Twin2StatusMode.HIDE))


            dst
                    .status(twinStatusRestDTOMapper.convertOrPostpone(src.getTwinStatus(), mapperContext.forkOnPoint(StatusMode.Twin2StatusMode.SHORT)))
                    .statusId(src.getTwinStatusId());



        if (mapperContext.hasModeButNot(UserMode.Twin2UserMode.HIDE)) {
            dst
                    .assignerUser(userDTOMapper.convertOrPostpone(src.getAssignerUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Twin2UserMode.SHORT))))
                    .authorUser(userDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Twin2UserMode.SHORT))))
                    .assignerUserId(src.getAssignerUserId())
                    .authorUserId(src.getCreatedByUserId());
        }
        if (mapperContext.hasModeButNot(TwinClassMode.Twin2TwinClassMode.HIDE))
            dst
                    .twinClass(twinClassRestDTOMapper.convertOrPostpone(src.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.Twin2TwinClassMode.SHORT))) //todo deep recursion risk
                    .twinClassId(src.getTwinClassId());
        if (mapperContext.hasModeButNot(RelationTwinMode.TwinByHeadMode.WHITE)) {
            twinService.loadHeadForTwin(src);
            dst
                    .headTwin(this.convertOrPostpone(src.getHeadTwin(), mapperContext.forkOnPoint(RelationTwinMode.TwinByHeadMode.GREEN)))  //head twin will be much less detail
                    .twinClassId(src.getTwinClassId());
        }
        if (mapperContext.hasModeButNot(TwinAliasMode.HIDE)) {
            twinAliasService.loadAliases(src);
            dst
                    .aliases(twinAliasRestDTOMapper.convertCollection(src.getTwinAliases().getCollection(), mapperContext));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinAliasMode.HIDE))
            twinAliasService.loadAliases(srcCollection);

        //todo load heads for collection
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return twinBaseRestDTOMapper.hideMode(mapperContext);
    }

    @Override
    public String getObjectCacheId(TwinEntity src) {
        return src.getId().toString();
    }
}
