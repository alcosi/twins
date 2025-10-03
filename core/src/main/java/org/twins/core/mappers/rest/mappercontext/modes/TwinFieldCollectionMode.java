package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.MapperMode;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum TwinFieldCollectionMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHOW(1),
    @FieldNameConstants.Include @Deprecated NO_FIELDS(2),
    @FieldNameConstants.Include @Deprecated NOT_EMPTY_FIELDS(3),
    @FieldNameConstants.Include @Deprecated ALL_FIELDS(4),
    @FieldNameConstants.Include @Deprecated NOT_EMPTY_FIELDS_WITH_ATTACHMENTS(5),
    @FieldNameConstants.Include @Deprecated ALL_FIELDS_WITH_ATTACHMENTS(6);

    final int priority;

    public static void legacyConverter(MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(TwinFieldCollectionMode.NO_FIELDS)) {
            case NO_FIELDS -> mapperContext
                    .setMode(TwinFieldCollectionMode.HIDE);
            case ALL_FIELDS -> mapperContext
                    .setMode(TwinFieldCollectionMode.SHOW)
                    .setMode(TwinFieldCollectionFilterEmptyMode.ANY);
            case NOT_EMPTY_FIELDS -> mapperContext
                    .setMode(TwinFieldCollectionMode.SHOW)
                    .setMode(TwinFieldCollectionFilterEmptyMode.ONLY_NOT);
            case ALL_FIELDS_WITH_ATTACHMENTS -> mapperContext
                    .setMode(TwinFieldCollectionMode.SHOW)
                    .setMode(TwinFieldCollectionFilterEmptyMode.ANY)
                    .setMode(AttachmentMode.TwinField2AttachmentMode.SHORT);
            case NOT_EMPTY_FIELDS_WITH_ATTACHMENTS -> mapperContext
                    .setMode(TwinFieldCollectionMode.SHOW)
                    .setMode(TwinFieldCollectionFilterEmptyMode.ONLY_NOT)
                    .setMode(AttachmentMode.TwinField2AttachmentMode.SHORT);
        }
    }
}
