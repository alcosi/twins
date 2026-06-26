package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.twin.TwinCountDTOv1;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TwinCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinEntity, UUID>, TwinCountDTOv1> {
    @MapperModePointerBinding(modes = {UserMode.Twin2UserMode.class})
    private final UserRestDTOMapper userDTOMapper;

    @MapperModePointerBinding(modes = {TwinClassMode.Twin2TwinClassMode.class})
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = {StatusMode.Twin2StatusMode.class})
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @MapperModePointerBinding(modes = {TwinMode.class})
    private final TwinBaseRestDTOMapper twinBaseRestDTOMapper;

    private final TwinService twinService;

    @Override
    public void map(CountResult<TwinEntity, UUID> src, TwinCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        TwinEntity entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        Set<UUID> groupFields = src.getGroupFields();
        if (groupFields != null) {
            if (groupFields.contains(SystemIds.TwinClassField.TWIN_TWIN_CLASS_ID))
                dst.setTwinClassId(entity.getTwinClassId());
            if (groupFields.contains(SystemIds.TwinClassField.TWIN_STATUS_ID))
                dst.setTwinStatusId(entity.getTwinStatusId());
            if (groupFields.contains(SystemIds.TwinClassField.TWIN_OWNER_USER_ID))
                dst.setOwnerUserId(entity.getOwnerUserId());
            if (groupFields.contains(SystemIds.TwinClassField.TWIN_ASSIGNEE_USER_ID))
                dst.setAssignerUserId(entity.getAssignerUserId());
            if (groupFields.contains(SystemIds.TwinClassField.TWIN_CREATOR_USER_ID))
                dst.setCreatedByUserId(entity.getCreatedByUserId());
            if (groupFields.contains(SystemIds.TwinClassField.TWIN_HEAD_ID))
                dst.setHeadTwinId(entity.getHeadTwinId());
        }
        dst.setCount(src.getCount());

        if (needLoad(mapperContext, StatusMode.Twin2StatusMode.HIDE, src, SystemIds.TwinClassField.TWIN_STATUS_ID)) {
            twinService.loadStatus(entity);
            twinStatusRestDTOMapper.postpone(entity.getTwinStatus(), mapperContext.forkOnPoint(StatusMode.Twin2StatusMode.SHORT));
        }
        if (needLoad(mapperContext, TwinClassMode.Twin2TwinClassMode.HIDE, src, SystemIds.TwinClassField.TWIN_TWIN_CLASS_ID)) {
            twinService.loadClass(entity);
            twinClassRestDTOMapper.postpone(entity.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.Twin2TwinClassMode.SHORT));
        }
        if (needLoad(mapperContext, TwinMode.HIDE, src, SystemIds.TwinClassField.TWIN_HEAD_ID)) {
            twinService.loadHead(entity);
            twinBaseRestDTOMapper.postpone(entity.getHeadTwin(), mapperContext);
        }
        if (needLoad(mapperContext, UserMode.Twin2UserMode.HIDE, src, SystemIds.TwinClassField.TWIN_OWNER_USER_ID, SystemIds.TwinClassField.TWIN_CREATOR_USER_ID, SystemIds.TwinClassField.TWIN_ASSIGNEE_USER_ID)) {
            twinService.loadUser(entity);
            userDTOMapper.convertOrPostpone(entity.getOwnerUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Twin2UserMode.SHORT)));
            userDTOMapper.convertOrPostpone(entity.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Twin2UserMode.SHORT)));
            userDTOMapper.convertOrPostpone(entity.getAssignerUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Twin2UserMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinEntity, UUID>> srcCollection, MapperContext mapperContext) throws Exception {
        var entities = srcCollection.stream().map(CountResult::getEntity).toList();
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, StatusMode.Twin2StatusMode.HIDE, someCount, SystemIds.TwinClassField.TWIN_STATUS_ID))
            twinService.loadStatus(entities);
        if (needLoad(mapperContext, TwinClassMode.Twin2TwinClassMode.HIDE, someCount, SystemIds.TwinClassField.TWIN_TWIN_CLASS_ID))
            twinService.loadClass(entities);
        if (needLoad(mapperContext, TwinMode.HIDE, someCount, SystemIds.TwinClassField.TWIN_HEAD_ID))
            twinService.loadHead(entities);
        if (needLoad(mapperContext, UserMode.Twin2UserMode.HIDE, someCount, SystemIds.TwinClassField.TWIN_OWNER_USER_ID, SystemIds.TwinClassField.TWIN_CREATOR_USER_ID, SystemIds.TwinClassField.TWIN_ASSIGNEE_USER_ID))
            twinService.loadUser(entities);
    }
}
