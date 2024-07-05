package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum TwinFieldCollectionMode implements MapperMode {
    @FieldNameConstants.Include NO_FIELDS(0),
    @FieldNameConstants.Include NOT_EMPTY_FIELDS(1),
    @FieldNameConstants.Include ALL_FIELDS(2),
    @FieldNameConstants.Include NOT_EMPTY_FIELDS_WITH_ATTACHMENTS(3),
    @FieldNameConstants.Include ALL_FIELDS_WITH_ATTACHMENTS(4);

    final int priority;
}
