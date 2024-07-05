package org.twins.core.mappers.rest.history;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dto.rest.history.HistoryDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twin.TwinBaseV2RestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.history.HistoryService;


@Component
@RequiredArgsConstructor
public class HistoryDTOMapperV1 extends RestSimpleDTOMapper<HistoryEntity, HistoryDTOv1> {

    @MapperModePointerBinding(modes = UserMode.HistoryOnUserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = TwinMode.HistoryOnTwinMode.class)
    private final TwinBaseV2RestDTOMapper twinBaseV2RestDTOMapper;

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

        if (mapperContext.hasModeButNot(UserMode.HistoryOnUserMode.HIDE))
            dst.actorUser(userRestDTOMapper.convertOrPostpone(src.getActorUser(), mapperContext
                    .forkOnPoint(mapperContext.getModeOrUse(UserMode.HistoryOnUserMode.SHORT))));
        if (mapperContext.hasModeButNot(TwinMode.HistoryOnTwinMode.HIDE))
            dst.twin(twinBaseV2RestDTOMapper.convertOrPostpone(src.getTwin(), mapperContext
                    .forkOnPoint(TwinMode.HistoryOnTwinMode.SHORT)));
    }

    @Override
    public String getObjectCacheId(HistoryEntity src) {
        return src.getId().toString();
    }
}
