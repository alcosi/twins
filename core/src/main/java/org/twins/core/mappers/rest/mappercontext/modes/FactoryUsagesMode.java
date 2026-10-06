package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum FactoryUsagesMode implements MapperModePointer<UsagesMode> {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Override
    public UsagesMode point() {
        return switch (this) {
            case HIDE -> UsagesMode.HIDE;
            case SHORT -> UsagesMode.SHORT;
            case DETAILED -> UsagesMode.DETAILED;
        };
    }
}
