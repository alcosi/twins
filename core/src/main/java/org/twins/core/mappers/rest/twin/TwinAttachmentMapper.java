package org.twins.core.mappers.rest.twin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.service.twin.TwinAttachmentService;


@Component
@RequiredArgsConstructor
public class TwinAttachmentMapper {
    final TwinAttachmentService twinAttachmentService;

    public String getObjectCacheId(TwinAttachmentEntity src) {
        return src.getId().toString();
    }

    @AllArgsConstructor
    public enum Mode implements MapperMode {
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
}
