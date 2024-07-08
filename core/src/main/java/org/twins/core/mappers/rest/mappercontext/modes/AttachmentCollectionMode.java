package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum AttachmentCollectionMode implements MapperMode {
    @FieldNameConstants.Include DIRECT(0),
    @FieldNameConstants.Include FROM_TRANSITIONS(1),
    @FieldNameConstants.Include FROM_COMMENTS(1),
    @FieldNameConstants.Include FROM_FIELDS(1),
    @FieldNameConstants.Include ALL(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Twin2AttachmentCollectionMode implements MapperModePointer<AttachmentCollectionMode> {
        @FieldNameConstants.Include DIRECT(0),
        @FieldNameConstants.Include FROM_TRANSITIONS(1),
        @FieldNameConstants.Include FROM_COMMENTS(1),
        @FieldNameConstants.Include FROM_FIELDS(1),
        @FieldNameConstants.Include ALL(2);

        final int priority;

        @Override
        public AttachmentCollectionMode point() {
            return switch (this) {
                case DIRECT -> AttachmentCollectionMode.DIRECT;
                case FROM_TRANSITIONS -> AttachmentCollectionMode.FROM_TRANSITIONS;
                case FROM_COMMENTS -> AttachmentCollectionMode.FROM_COMMENTS;
                case FROM_FIELDS -> AttachmentCollectionMode.FROM_FIELDS;
                case ALL -> AttachmentCollectionMode.ALL;
            };
        }
    }
}
