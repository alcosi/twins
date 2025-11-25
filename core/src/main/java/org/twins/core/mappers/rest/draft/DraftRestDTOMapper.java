package org.twins.core.mappers.rest.draft;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.domain.draft.DraftCounters;
import org.twins.core.dto.rest.draft.DraftDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DraftMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.draft.DraftCounterService;

import static org.twins.core.domain.draft.DraftCounters.Counter.*;
import static org.twins.core.domain.draft.DraftCounters.CounterGroup.ERASES;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {DraftMode.class})
public class DraftRestDTOMapper extends RestSimpleDTOMapper<DraftEntity, DraftDTOv1> {
    @Lazy
    @Autowired
    private final DraftCounterService draftCounterService;

    @MapperModePointerBinding(modes = UserMode.Draft2UserMode.class)
    final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(DraftEntity src, DraftDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DraftMode.SHORT)) {
            case DETAILED:
                draftCounterService.loadCounters(src);
                DraftCounters counters = src.getCounters();
                dst
                        .setId(src.getId())
                        .setStatus(src.getStatus())
                        .setCreatedByrUserId(src.getCreatedByUserId())
                        .setTwinCreateCount(counters.useNullForZero(PERSIST_CREATE))
                        .setTwinUpdateCount(counters.useNullForZero(PERSIST_UPDATE))
                        .setTwinDeleteCount(counters.useNullForZero(ERASES))
                        .setTwinDeletedByStatusCount(counters.useNullForZero(ERASE_BY_STATUS))
                        .setTwinDeletedIrrevocableCount(counters.useNullForZero(ERASE_IRREVOCABLE_HANDLED))
                        .setTwinTagCreateCount(counters.useNullForZero(TAG_CREATE))
                        .setTwinTagDeleteCount(counters.useNullForZero(TAG_DELETE))
                        .setTwinMarkerCreateCount(counters.useNullForZero(MARKER_CREATE))
                        .setTwinMarkerDeleteCount(counters.useNullForZero(MARKER_DELETE))
                        .setTwinLinkCreateCount(counters.useNullForZero(LINK_CREATE))
                        .setTwinLinkDeleteCount(counters.useNullForZero(LINK_DELETE))
                        .setTwinLinkUpdateCount(counters.useNullForZero(LINK_UPDATE))
                        .setTwinAttachmentCreateCount(counters.useNullForZero(ATTACHMENT_CREATE))
                        .setTwinAttachmentDeleteCount(counters.useNullForZero(ATTACHMENT_DELETE))
                        .setTwinAttachmentUpdateCount(counters.useNullForZero(ATTACHMENT_UPDATE))
                        .setTwinFieldSimpleCreateCount(counters.useNullForZero(FIELD_SIMPLE_CREATE))
                        .setTwinFieldSimpleUpdateCount(counters.useNullForZero(FIELD_SIMPLE_UPDATE))
                        .setTwinFieldSimpleDeleteCount(counters.useNullForZero(FIELD_SIMPLE_DELETE))
                        .setTwinFieldUserCreateCount(counters.useNullForZero(FIELD_USER_CREATE))
                        .setTwinFieldUserUpdateCount(counters.useNullForZero(FIELD_USER_UPDATE))
                        .setTwinFieldUserDeleteCount(counters.useNullForZero(FIELD_USER_DELETE))
                        .setTwinFieldDataListCreateCount(counters.useNullForZero(FIELD_DATALIST_CREATE))
                        .setTwinFieldDataListUpdateCount(counters.useNullForZero(FIELD_DATALIST_UPDATE))
                        .setTwinFieldDataListDeleteCount(counters.useNullForZero(FIELD_DATALIST_DELETE))
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setStatus(src.getStatus());
                break;
        }
        if (mapperContext.hasModeButNot(UserMode.Draft2UserMode.HIDE)) {
            dst.setCreatedByrUserId(src.getCreatedByUserId());
            userRestDTOMapper.postpone(src.getCreatedByUser(), mapperContext.forkOnPoint(UserMode.Draft2UserMode.SHORT));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(DraftMode.HIDE);
    }

    @Override
    public String getObjectCacheId(DraftEntity src) {
        return src.getId().toString();
    }

}
