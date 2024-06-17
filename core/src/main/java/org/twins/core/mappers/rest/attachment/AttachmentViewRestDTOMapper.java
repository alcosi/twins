package org.twins.core.mappers.rest.attachment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentViewDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperModePointer;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TransitionBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.twin.TwinAttachmentService;
import org.twins.core.service.twin.TwinService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class AttachmentViewRestDTOMapper extends RestSimpleDTOMapper<TwinAttachmentEntity, AttachmentViewDTOv1> {
    final UserRestDTOMapper userDTOMapper;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    final TransitionBaseV1RestDTOMapper transitionRestDTOMapper;
    @Autowired
    TwinClassRestDTOMapper twinClassRestDTOMapper;
    final TwinFieldRestDTOMapper twinFieldRestDTOMapper;
    final TwinService twinService;
    final TwinAttachmentService twinAttachmentService;

    @Override
    public void map(TwinAttachmentEntity src, AttachmentViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(AttachmentViewRestDTOMapper.Mode.DETAILED)) {
            case DETAILED:
                dst
                        .setAuthorUserId(src.getCreatedByUserId())
                        .setAuthorUser(userDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.cloneWithIsolatedModes().setMode(UserRestDTOMapper.Mode.SHORT)))
                        .setTwinflowTransitionId(src.getTwinflowTransitionId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setTwinClassFieldId(src.getTwinClassFieldId())
                        .setCommentId(src.getTwinCommentId())
                        .setDescription(src.getDescription())
                        .setTitle(src.getTitle())
                        .setExternalId(src.getExternalId());
            case SHORT:
                dst
                        .setId(src.getId())
                        .setStorageLink(src.getStorageLink());
        }
        if (!mapperContext.hasModeOrEmpty(AttachmentViewRestDTOMapper.Mode.HIDE)) {
            dst
                    .setTwinflowTransitionId(src.getTwinflowTransitionId())
                    .setTwinflowTransition(transitionRestDTOMapper.convertOrPostpone(src.getTwinflowTransition(), mapperContext.forkOnPoint(AttachmentTransitionMode.SHORT)));
        }
    }

    @Override
    public List<AttachmentViewDTOv1> convertList(Collection<TwinAttachmentEntity> srcList, MapperContext mapperContext) throws Exception {
        Collection<TwinAttachmentEntity> newList = new ArrayList<>();
        switch (mapperContext.getModeOrUse(TwinAttachmentMode.ALL)) {
            case DIRECT:
                newList = srcList.stream().filter(twinAttachmentService::checkOnDirect).collect(Collectors.toList());
                break;
            case FROM_TRANSITIONS:
                newList = srcList.stream().filter(el -> el.getTwinflowTransitionId() != null).collect(Collectors.toList());
                break;
            case FROM_COMMENTS:
                newList = srcList.stream().filter(el -> el.getTwinCommentId() != null).collect(Collectors.toList());
                break;
            case FROM_FIELDS:
                newList = srcList.stream().filter(el -> el.getTwinClassFieldId() != null).collect(Collectors.toList());
                break;
            case ALL:
                newList = srcList;
                break;
        }
        return super.convertList(newList, mapperContext);
    }

    @Override
    public String getObjectCacheId(TwinAttachmentEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(AttachmentViewRestDTOMapper.Mode.HIDE);
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

    @AllArgsConstructor
    public enum TwinAttachmentMode implements MapperMode {
        DIRECT(0),
        FROM_TRANSITIONS(1),
        FROM_COMMENTS(1),
        FROM_FIELDS(1),
        ALL(2);

        public static final String _ALL = "ALL";
        public static final String _DIRECT = "DIRECT";
        public static final String _FROM_TRANSITIONS = "FROM_TRANSITIONS";
        public static final String _FROM_COMMENTS = "FROM_COMMENTS";
        public static final String _FROM_FIELDS = "FROM_FIELDS";

        @Getter
        final int priority;
    }

    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum AttachmentTransitionMode implements MapperModePointer<TransitionBaseV1RestDTOMapper.Mode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        @Getter
        final int priority;

        @Override
        public TransitionBaseV1RestDTOMapper.Mode point() {
            return switch (this) {
                case HIDE -> TransitionBaseV1RestDTOMapper.Mode.HIDE;
                case SHORT -> TransitionBaseV1RestDTOMapper.Mode.SHORT;
                case DETAILED -> TransitionBaseV1RestDTOMapper.Mode.DETAILED;
            };
        }
    }

}
