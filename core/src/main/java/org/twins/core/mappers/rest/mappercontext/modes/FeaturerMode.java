package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum FeaturerMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinClass2FeaturerMode implements MapperModePointer<FeaturerMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FeaturerMode point() {
            return switch (this) {
                case HIDE -> FeaturerMode.HIDE;
                case SHORT -> FeaturerMode.SHORT;
                case DETAILED -> FeaturerMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinClassField2FeaturerMode implements MapperModePointer<FeaturerMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FeaturerMode point() {
            return switch (this) {
                case HIDE -> FeaturerMode.HIDE;
                case SHORT -> FeaturerMode.SHORT;
                case DETAILED -> FeaturerMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Trigger2FeaturerMode implements MapperModePointer<FeaturerMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FeaturerMode point() {
            return switch (this) {
                case HIDE -> FeaturerMode.HIDE;
                case SHORT -> FeaturerMode.SHORT;
                case DETAILED -> FeaturerMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinValidator2FeaturerMode implements MapperModePointer<FeaturerMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FeaturerMode point() {
            return switch (this) {
                case HIDE -> FeaturerMode.HIDE;
                case SHORT -> FeaturerMode.SHORT;
                case DETAILED -> FeaturerMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FactoryPipelineStep2FeaturerMode implements MapperModePointer<FeaturerMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FeaturerMode point() {
            return switch (this) {
                case HIDE -> FeaturerMode.HIDE;
                case SHORT -> FeaturerMode.SHORT;
                case DETAILED -> FeaturerMode.DETAILED;
            };
        }

    }@Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FactoryCondition2FeaturerMode implements MapperModePointer<FeaturerMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FeaturerMode point() {
            return switch (this) {
                case HIDE -> FeaturerMode.HIDE;
                case SHORT -> FeaturerMode.SHORT;
                case DETAILED -> FeaturerMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FactoryMultiplier2FeaturerMode implements MapperModePointer<FeaturerMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FeaturerMode point() {
            return switch (this) {
                case HIDE -> FeaturerMode.HIDE;
                case SHORT -> FeaturerMode.SHORT;
                case DETAILED -> FeaturerMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum DomainUserGroupManager2FeaturerMode implements MapperModePointer<FeaturerMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FeaturerMode point() {
            return switch (this) {
                case HIDE -> FeaturerMode.HIDE;
                case SHORT -> FeaturerMode.SHORT;
                case DETAILED -> FeaturerMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TransitionTrigger2FeaturerMode implements MapperModePointer<FeaturerMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FeaturerMode point() {
            return switch (this) {
                case HIDE -> FeaturerMode.HIDE;
                case SHORT -> FeaturerMode.SHORT;
                case DETAILED -> FeaturerMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Projection2FeaturerMode implements MapperModePointer<FeaturerMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FeaturerMode point() {
            return switch (this) {
                case HIDE -> FeaturerMode.HIDE;
                case SHORT -> FeaturerMode.SHORT;
                case DETAILED -> FeaturerMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Scheduler2FeaturerMode implements MapperModePointer<FeaturerMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FeaturerMode point() {
            return switch (this) {
                case HIDE -> FeaturerMode.HIDE;
                case SHORT -> FeaturerMode.SHORT;
                case DETAILED -> FeaturerMode.DETAILED;
            };
        }
    }
}
