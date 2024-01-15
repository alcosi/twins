package org.twins.core.mappers.rest.attachment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentViewDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinTransitionRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.twin.TwinService;


@Component
@RequiredArgsConstructor
public class AttachmentViewRestDTOMapper extends RestSimpleDTOMapper<TwinAttachmentEntity, AttachmentViewDTOv1> {
    final UserRestDTOMapper userDTOMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    final TwinTransitionRestDTOMapper twinTransitionRestDTOMapper;
    @Autowired
    TwinClassRestDTOMapper twinClassRestDTOMapper;
    final TwinFieldRestDTOMapper twinFieldRestDTOMapper;
    final TwinService twinService;

    @Override
    public void map(TwinAttachmentEntity src, AttachmentViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dst
                        .setAuthorUserId(src.getCreatedByUserId())
                        .setAuthorUser(userDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.cloneWithIsolatedModes().setMode(UserRestDTOMapper.Mode.SHORT)))
                        .setTwinflowTransitionId(src.getTwinflowTransitionId())
                        .setTwinflowTransition(twinTransitionRestDTOMapper.convertOrPostpone(src.getTwinflowTransition(), mapperContext.cloneWithIsolatedModes().setMode(TwinTransitionRestDTOMapper.Mode.SHORT)))
                        .setCreatedAt(src.getCreatedAt().toInstant())
                        .setDescription(src.getDescription())
                        .setTitle(src.getTitle())
                        .setExternalId(src.getExternalId());
            case SHORT:
                dst
                        .setId(src.getId())
                        .setStorageLink(src.getStorageLink());
        }
    }

    @Override
    public String getObjectCacheId(TwinAttachmentEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(Mode.HIDE);
    }

    @AllArgsConstructor
    public enum Mode implements MapperMode {
        HIDE(0),
        SHORT(1),
        DETAILED(2);

        public static final String _HIDE = "HIDE";
        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";

        @Getter
        final int priority;
    }
}
