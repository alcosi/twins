package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum FactoryMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FactoryPipeline2FactoryMode implements MapperModePointer<FactoryMode> {
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
    public enum FactoryPipelineNextTwinFactory2FactoryMode implements MapperModePointer<FactoryMode> {
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
    public enum FactoryMultiplier2FactoryMode implements MapperModePointer<FactoryMode> {
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
    public enum FactoryBranch2FactoryMode implements MapperModePointer<FactoryMode> {
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
    public enum FactoryEraser2FactoryMode implements MapperModePointer<FactoryMode> {
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
    public enum Transition2FactoryMode implements MapperModePointer<FactoryMode> {
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
    public enum TwinflowFactory2FactoryMode implements MapperModePointer<FactoryMode> {
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
}
