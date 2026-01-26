package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum StatusMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Transition2StatusMode implements MapperModePointer<StatusMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public StatusMode point() {
            return switch (this) {
                case HIDE -> StatusMode.HIDE;
                case SHORT -> StatusMode.SHORT;
                case DETAILED -> StatusMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinClass2StatusMode implements MapperModePointer<StatusMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public StatusMode point() {
            return switch (this) {
                case HIDE -> StatusMode.HIDE;
                case SHORT -> StatusMode.SHORT;
                case DETAILED -> StatusMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Twin2StatusMode implements MapperModePointer<StatusMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public StatusMode point() {
            return switch (this) {
                case HIDE -> StatusMode.HIDE;
                case SHORT -> StatusMode.SHORT;
                case DETAILED -> StatusMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinClassFreeze2StatusMode implements MapperModePointer<StatusMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public StatusMode point() {
            return switch (this) {
                case HIDE -> StatusMode.HIDE;
                case SHORT -> StatusMode.SHORT;
                case DETAILED -> StatusMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinField2StatusMode implements MapperModePointer<StatusMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public StatusMode point() {
            return switch (this) {
                case HIDE -> StatusMode.HIDE;
                case SHORT -> StatusMode.SHORT;
                case DETAILED -> StatusMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinflowInitStatus2StatusMode implements MapperModePointer<StatusMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public StatusMode point() {
            return switch (this) {
                case HIDE -> StatusMode.HIDE;
                case SHORT -> StatusMode.SHORT;
                case DETAILED -> StatusMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum PropagationTwinStatus2StatusMode implements MapperModePointer<StatusMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public StatusMode point() {
            return switch (this) {
                case HIDE -> StatusMode.HIDE;
                case SHORT -> StatusMode.SHORT;
                case DETAILED -> StatusMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FactoryPipelineOutputTwinStatus2StatusMode implements MapperModePointer<StatusMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public StatusMode point() {
            return switch (this) {
                case HIDE -> StatusMode.HIDE;
                case SHORT -> StatusMode.SHORT;
                case DETAILED -> StatusMode.DETAILED;
            };
        }
    }
}
