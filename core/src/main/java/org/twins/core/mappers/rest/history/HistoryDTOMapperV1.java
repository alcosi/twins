package org.twins.core.mappers.rest.history;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dto.rest.history.HistoryDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twin.TwinBaseRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.history.HistoryService;


@Component
@RequiredArgsConstructor
public class HistoryDTOMapperV1 extends RestSimpleDTOMapper<HistoryEntity, HistoryDTOv1> {

    @MapperModePointerBinding(modes = UserMode.History2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = TwinMode.History2TwinMode.class)
    private final TwinBaseRestDTOMapper twinBaseV2RestDTOMapper;

    private final HistoryService historyService;

    @Override
    public void map(HistoryEntity src, HistoryDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .changeDescription(historyService.getChangeFreshestDescription(src))
                .actorUserId(src.getActorUserId())
                .twinId(src.getTwin().getId())
                .batchId(src.getHistoryBatchId())
                .type(src.getHistoryType())
                .id(src.getId())
                .createdAt(src.getCreatedAt().toLocalDateTime());

        if (mapperContext.hasModeButNot(UserMode.History2UserMode.HIDE))
            dst.actorUser(userRestDTOMapper.convertOrPostpone(src.getActorUser(), mapperContext
                    .forkOnPoint(mapperContext.getModeOrUse(UserMode.History2UserMode.SHORT))));
        if (mapperContext.hasModeButNot(TwinMode.History2TwinMode.HIDE))
            dst.twin(twinBaseV2RestDTOMapper.convertOrPostpone(src.getTwin(), mapperContext
                    .forkOnPoint(TwinMode.History2TwinMode.SHORT)));
    }

    @Override
    public String getObjectCacheId(HistoryEntity src) {
        return src.getId().toString();
    }
}
