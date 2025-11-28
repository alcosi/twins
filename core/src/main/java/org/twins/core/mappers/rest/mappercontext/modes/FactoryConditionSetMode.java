package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum FactoryConditionSetMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FactoryPipeline2FactoryConditionSetMode implements MapperModePointer<FactoryConditionSetMode> {
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
    public enum FactoryPipelineStep2FactoryConditionSetMode implements MapperModePointer<FactoryConditionSetMode> {
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
    public enum FactoryMultiplierFilter2FactoryConditionSetMode implements MapperModePointer<FactoryConditionSetMode> {
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
    public enum FactoryBranch2FactoryConditionSetMode implements MapperModePointer<FactoryConditionSetMode> {
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
    public enum FactoryCondition2FactoryConditionSetMode implements MapperModePointer<FactoryConditionSetMode> {
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
    public enum FactoryEraser2FactoryConditionSetMode implements MapperModePointer<FactoryConditionSetMode> {
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
}
