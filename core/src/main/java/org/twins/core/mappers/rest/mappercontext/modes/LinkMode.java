package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum LinkMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2),
    @FieldNameConstants.Include MANAGED(3);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinClass2LinkMode implements MapperModePointer<LinkMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);

        final int priority;

        @Override
        public LinkMode point() {
            return switch (this) {
                case HIDE -> LinkMode.HIDE;
                case SHORT -> LinkMode.SHORT;
                case DETAILED -> LinkMode.DETAILED;
                case MANAGED -> LinkMode.MANAGED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinLink2LinkMode implements MapperModePointer<LinkMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);

        final int priority;

        @Override
        public LinkMode point() {
            return switch (this) {
                case HIDE -> LinkMode.HIDE;
                case SHORT -> LinkMode.SHORT;
                case DETAILED -> LinkMode.DETAILED;
                case MANAGED -> LinkMode.MANAGED;
            };
        }
    }
}
