package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum TwinFieldCollectionFilterEmptyMode implements MapperMode {
    @FieldNameConstants.Include ONLY_NOT(0),
    @FieldNameConstants.Include ONLY(1),
    @FieldNameConstants.Include ANY(1);

    final int priority;
}
