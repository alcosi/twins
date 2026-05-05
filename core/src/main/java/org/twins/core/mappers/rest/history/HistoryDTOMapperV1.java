package org.twins.core.mappers.rest.history;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dto.rest.history.HistoryDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.HistoryTypeMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.history.HistoryService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class HistoryDTOMapperV1 extends RestSimpleDTOMapper<HistoryEntity, HistoryDTOv1> {
    @MapperModePointerBinding(modes = UserMode.History2UserMode.class)
    private final UserRestDTOMapper userDTOMapper;
    @MapperModePointerBinding(modes = TwinMode.History2TwinMode.class)
    private final TwinRestDTOMapperV2 twinDTOMapper;
    @MapperModePointerBinding(modes = TwinClassFieldMode.History2TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldDTOMapper;
    private final HistoryService historyService;

    @Override
    public void map(HistoryEntity src, HistoryDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(HistoryTypeMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setTwinId(src.getTwinId())
                    .setBatchId(src.getHistoryBatchId())
                    .setActorUserId(src.getActorUserId())
                    .setMachineUserId(src.getMachineUserId())
                    .setTwinClassFieldId(src.getTwinClassFieldId())
                    .setType(src.getHistoryType())
                    .setChangeDescription(historyService.getChangeFreshestDescription(src))
                    .setCreatedAt(src.getCreatedAt().toLocalDateTime());
            case SHORT -> dst
                    .setId(src.getId());
        }
        if (mapperContext.hasModeButNot(UserMode.History2UserMode.HIDE)) {
            historyService.loadUser(src);
            historyService.loadMachineUser(src);
            userDTOMapper.convertOrPostpone(src.getActorUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.History2UserMode.SHORT)));
            userDTOMapper.convertOrPostpone(src.getMachineUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.History2UserMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(TwinMode.History2TwinMode.HIDE)) {
            twinDTOMapper.postpone(src.getTwin(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinMode.History2TwinMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(TwinClassFieldMode.History2TwinClassFieldMode.HIDE)) {
            historyService.loadTwinClassField(src);
            twinClassFieldDTOMapper.postpone(src.getTwinClassField(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassFieldMode.History2TwinClassFieldMode.SHORT)));
        }
    }

    @Override
    public String getObjectCacheId(HistoryEntity src) {
        return src.getId().toString();
    }

    @Override
    public void beforeCollectionConversion(Collection<HistoryEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(UserMode.History2UserMode.HIDE)) {
            historyService.loadUser(srcCollection);
            historyService.loadMachineUser(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinClassFieldMode.History2TwinClassFieldMode.HIDE)) {
            historyService.loadTwinClassField(srcCollection);
        }
    }
}

