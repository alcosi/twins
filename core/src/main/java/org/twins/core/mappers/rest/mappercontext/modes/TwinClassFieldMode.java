package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum TwinClassFieldMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2),
    @FieldNameConstants.Include MANAGED(3);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinClass2TwinClassFieldMode implements MapperModePointer<TwinClassFieldMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);

        final int priority;

        @Override
        public TwinClassFieldMode point() {
            return switch (this) {
                case HIDE -> TwinClassFieldMode.HIDE;
                case SHORT -> TwinClassFieldMode.SHORT;
                case DETAILED -> TwinClassFieldMode.DETAILED;
                case MANAGED -> TwinClassFieldMode.MANAGED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinField2TwinClassFieldMode implements MapperModePointer<TwinClassFieldMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);


        final int priority;

        @Override
        public TwinClassFieldMode point() {
            return switch (this) {
                case HIDE -> TwinClassFieldMode.HIDE;
                case SHORT -> TwinClassFieldMode.SHORT;
                case DETAILED -> TwinClassFieldMode.DETAILED;
                case MANAGED -> TwinClassFieldMode.MANAGED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Attachment2TwinClassFieldMode implements MapperModePointer<TwinClassFieldMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);


        final int priority;

        @Override
        public TwinClassFieldMode point() {
            return switch (this) {
                case HIDE -> TwinClassFieldMode.HIDE;
                case SHORT -> TwinClassFieldMode.SHORT;
                case DETAILED -> TwinClassFieldMode.DETAILED;
                case MANAGED -> TwinClassFieldMode.MANAGED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinClassFieldCondition2TwinClassFieldMode implements MapperModePointer<TwinClassFieldMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);


        final int priority;

        @Override
        public TwinClassFieldMode point() {
            return switch (this) {
                case HIDE -> TwinClassFieldMode.HIDE;
                case SHORT -> TwinClassFieldMode.SHORT;
                case DETAILED -> TwinClassFieldMode.DETAILED;
                case MANAGED -> TwinClassFieldMode.MANAGED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinRule2TwinClassFieldMode implements MapperModePointer<TwinClassFieldMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinClassFieldMode point() {
            return switch (this) {
                case HIDE -> TwinClassFieldMode.HIDE;
                case SHORT -> TwinClassFieldMode.SHORT;
                case DETAILED -> TwinClassFieldMode.DETAILED;
            };
        }
    }
    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Projection2TwinClassFieldMode implements MapperModePointer<TwinClassFieldMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);


        final int priority;

        @Override
        public TwinClassFieldMode point() {
            return switch (this) {
                case HIDE -> TwinClassFieldMode.HIDE;
                case SHORT -> TwinClassFieldMode.SHORT;
                case DETAILED -> TwinClassFieldMode.DETAILED;
                case MANAGED -> TwinClassFieldMode.MANAGED;
            };
        }
    }

}
