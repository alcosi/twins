package org.twins.core.mappers.rest.history;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dto.rest.history.HistoryDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twin.TwinBaseV2RestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapperV2;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.twin.TwinService;


@Component
@RequiredArgsConstructor
public class HistoryDTOMapperV1 extends RestSimpleDTOMapper<HistoryEntity, HistoryDTOv1> {
    final UserRestDTOMapper userRestDTOMapper;
    final TwinBaseV2RestDTOMapper twinBaseV2RestDTOMapper;
    final TwinFieldRestDTOMapperV2 twinFieldRestDTOMapperV2;
    final TwinService twinService;
    final AuthService authService;
    final HistoryService historyService;

    @Override
    public void map(HistoryEntity src, HistoryDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .changeDescription(historyService.getChangeFreshestDescription(src))
                .actorUser(userRestDTOMapper.convertOrPostpone(src.getActorUser(), mapperContext))
                .twin(twinBaseV2RestDTOMapper.convertOrPostpone(src.getTwin(), mapperContext.cloneWithIsolatedModes()))
                .actorUserId(src.getActorUserId())
                .twinId(src.getTwin().getId())
                .type(src.getHistoryType())
                .id(src.getId())
                .createdAt(src.getCreatedAt().toLocalDateTime());
    }

    @Override
    public String getObjectCacheId(HistoryEntity src) {
        return src.getId().toString();
    }
}
