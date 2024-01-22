package org.twins.core.mappers.rest.history;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinHistoryItem;
import org.twins.core.dto.rest.history.HistoryDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twin.TwinBaseRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapperV2;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.time.LocalDateTime;
import java.time.ZoneId;


@Component
@RequiredArgsConstructor
public class HistoryDTOMapperV1 extends RestSimpleDTOMapper<TwinHistoryItem, HistoryDTOv1> {
    final UserRestDTOMapper userRestDTOMapper;
    final TwinBaseRestDTOMapper twinBaseRestDTOMapper;
    final TwinFieldRestDTOMapperV2 twinFieldRestDTOMapperV2;
    final TwinService twinService;
    final AuthService authService;

    @Override
    public void map(TwinHistoryItem src, HistoryDTOv1 dst, MapperContext mapperContext) throws Exception {
        ApiUser apiUser = authService.getApiUser();
        dst
                .actorUser(userRestDTOMapper.convertOrPostpone(apiUser.getUser(), mapperContext))
                .twin(twinBaseRestDTOMapper.convertOrPostpone(src.getTwin(), mapperContext))
                .actorUserId(apiUser.getUser().getId())
                .twinId(src.getTwin().getId())
                .type(src.getType())
                .createdAt(LocalDateTime.ofInstant(src.getCreatedAt(), ZoneId.systemDefault()));
        switch (dst.type) {
            case twinCreated:
                dst.changeDescription("Twin was created");
            case statusChanged:
                dst.changeDescription("Status was changed");
            case assigneeChanged:
                dst.changeDescription("Assignee was changed");
            case nameChanged:
                dst.changeDescription("Name was changed");
            default:
                dst.changeDescription("Some data was changed");
        }
    }

    @Override
    public String getObjectCacheId(TwinHistoryItem src) {
        return src.getId().toString();
    }
}
