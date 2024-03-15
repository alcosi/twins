package org.twins.core.mappers.rest.twin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.service.twin.TwinAttachmentService;

import java.util.List;


@Component
@RequiredArgsConstructor
public class TwinAttachmentMapper {
    final TwinAttachmentService twinAttachmentService;

    public List<TwinAttachmentEntity> map(List<TwinAttachmentEntity> srcCollection, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinAttachmentMapper.Mode.ALL)) {
            case DIRECT:
                srcCollection.removeIf(twinAttachmentService::checkOnDirect);
                break;
            case FROM_TRANSITIONS:
                srcCollection.removeIf(el -> el.getTwinflowTransitionId() == null);
                break;
            case FROM_COMMENTS:
                srcCollection.removeIf(el -> el.getTwinCommentId() == null);
                break;
            case FROM_FIELDS:
                srcCollection.removeIf(el -> el.getTwinClassFieldId() == null);
        }
        return srcCollection;
    }

    public String getObjectCacheId(TwinAttachmentEntity src) {
        return src.getId().toString();
    }

    public boolean noneMode(MapperContext mapperContext) {
        boolean resultCheckOnNone = mapperContext.hasModeOrEmpty(Mode.NONE);
        if (resultCheckOnNone)
            mapperContext.setMode(AttachmentViewRestDTOMapper.Mode.HIDE);
        return resultCheckOnNone;
    }

    @AllArgsConstructor
    public enum Mode implements MapperMode {
        NONE(0),
        ALL(1),
        DIRECT(3),
        FROM_TRANSITIONS(4),
        FROM_COMMENTS(5),
        FROM_FIELDS(6);

        public static final String _NONE = "NONE";
        public static final String _ALL = "ALL";
        public static final String _DIRECT = "DIRECT";
        public static final String _FROM_TRANSITIONS = "FROM_TRANSITIONS";
        public static final String _FROM_COMMENTS = "FROM_COMMENTS";
        public static final String _FROM_FIELDS = "FROM_FIELDS";

        @Getter
        final int priority;
    }
}
