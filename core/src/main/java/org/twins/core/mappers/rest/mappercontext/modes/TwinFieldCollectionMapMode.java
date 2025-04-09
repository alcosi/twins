package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum TwinFieldCollectionMapMode implements MapperMode {
    @FieldNameConstants.Include KEY(0),
    @FieldNameConstants.Include ID(1);

    final int priority;
}
