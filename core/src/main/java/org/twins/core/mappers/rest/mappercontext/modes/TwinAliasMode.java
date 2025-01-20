package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum TwinAliasMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include D(1),
    @FieldNameConstants.Include C(1),
    @FieldNameConstants.Include B(1),
    @FieldNameConstants.Include S(1),
    @FieldNameConstants.Include T(1),
    @FieldNameConstants.Include K(1),
    @FieldNameConstants.Include ALL(2);

    final int priority;
}
