package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum FactoryTriggerMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Factory2FactoryTriggerMode implements MapperModePointer<FactoryTriggerMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FactoryTriggerMode point() {
            return switch (this) {
                case HIDE -> FactoryTriggerMode.HIDE;
                case SHORT -> FactoryTriggerMode.SHORT;
                case DETAILED -> FactoryTriggerMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FactoryTrigger2FactoryMode implements MapperModePointer<FactoryMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FactoryMode point() {
            return switch (this) {
                case HIDE -> FactoryMode.HIDE;
                case SHORT -> FactoryMode.SHORT;
                case DETAILED -> FactoryMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FactoryTrigger2FactoryConditionSetMode implements MapperModePointer<FactoryConditionSetMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FactoryConditionSetMode point() {
            return switch (this) {
                case HIDE -> FactoryConditionSetMode.HIDE;
                case SHORT -> FactoryConditionSetMode.SHORT;
                case DETAILED -> FactoryConditionSetMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FactoryTrigger2TwinTriggerMode implements MapperModePointer<TwinTriggerMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinTriggerMode point() {
            return switch (this) {
                case HIDE -> TwinTriggerMode.HIDE;
                case SHORT -> TwinTriggerMode.SHORT;
                case DETAILED -> TwinTriggerMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FactoryTrigger2TwinClassMode implements MapperModePointer<TwinClassMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinClassMode point() {
            return switch (this) {
                case HIDE -> TwinClassMode.HIDE;
                case SHORT -> TwinClassMode.SHORT;
                case DETAILED -> TwinClassMode.DETAILED;
            };
        }
    }
}
