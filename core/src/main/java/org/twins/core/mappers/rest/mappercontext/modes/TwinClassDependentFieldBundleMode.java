package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum TwinClassDependentFieldBundleMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinField2TwinClassDependentFieldBundleMode implements MapperModePointer<TwinClassDependentFieldBundleMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinClassDependentFieldBundleMode point() {
            return switch (this) {
                case HIDE -> TwinClassDependentFieldBundleMode.HIDE;
                case SHORT -> TwinClassDependentFieldBundleMode.SHORT;
                case DETAILED -> TwinClassDependentFieldBundleMode.DETAILED;
            };
        }
    }

}
