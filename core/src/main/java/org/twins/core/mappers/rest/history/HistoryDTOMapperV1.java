package org.twins.core.mappers.rest.history;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dto.rest.history.HistoryDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twin.TwinBaseV2RestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.history.HistoryService;


@Component
@RequiredArgsConstructor
public class HistoryDTOMapperV1 extends RestSimpleDTOMapper<HistoryEntity, HistoryDTOv1> {

    @MapperModePointerBinding(modes = MapperMode.HistoryOnUserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = MapperMode.HistoryOnTwinMode.class)
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

        if (mapperContext.hasModeButNot(MapperMode.HistoryOnUserMode.HIDE))
            dst.actorUser(userRestDTOMapper.convertOrPostpone(src.getActorUser(), mapperContext
                    .forkOnPoint(mapperContext.getModeOrUse(MapperMode.HistoryOnUserMode.SHORT))));
        if (mapperContext.hasModeButNot(MapperMode.HistoryOnTwinMode.HIDE))
            dst.twin(twinBaseV2RestDTOMapper.convertOrPostpone(src.getTwin(), mapperContext
                    .forkOnPoint(MapperMode.HistoryOnTwinMode.SHORT)));
    }

    @Override
    public String getObjectCacheId(HistoryEntity src) {
        return src.getId().toString();
    }
}
