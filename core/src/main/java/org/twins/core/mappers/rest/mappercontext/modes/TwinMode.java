package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum TwinMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Touch2TwinMode implements MapperModePointer<TwinMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinMode point() {
            return switch (this) {
                case HIDE -> TwinMode.HIDE;
                case SHORT -> TwinMode.SHORT;
                case DETAILED -> TwinMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum History2TwinMode implements MapperModePointer<TwinMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinMode point() {
            return switch (this) {
                case HIDE -> TwinMode.HIDE;
                case SHORT -> TwinMode.SHORT;
                case DETAILED -> TwinMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinClassFieldDescriptor2TwinMode implements MapperModePointer<TwinMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinMode point() {
            return switch (this) {
                case HIDE -> TwinMode.HIDE;
                case SHORT -> TwinMode.SHORT;
                case DETAILED -> TwinMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Attachment2TwinMode implements MapperModePointer<TwinMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinMode point() {
            return switch (this) {
                case HIDE -> TwinMode.HIDE;
                case SHORT -> TwinMode.SHORT;
                case DETAILED -> TwinMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FaceTwidget2TwinMode implements MapperModePointer<TwinMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinMode point() {
            return switch (this) {
                case HIDE -> TwinMode.HIDE;
                case SHORT -> TwinMode.SHORT;
                case DETAILED -> TwinMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum DomainBusinessAccountTemplate2TwinMode implements MapperModePointer<TwinMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinMode point() {
            return switch (this) {
                case HIDE -> TwinMode.HIDE;
                case SHORT -> TwinMode.SHORT;
                case DETAILED -> TwinMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum DomainUserTemplate2TwinMode implements MapperModePointer<TwinMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinMode point() {
            return switch (this) {
                case HIDE -> TwinMode.HIDE;
                case SHORT -> TwinMode.SHORT;
                case DETAILED -> TwinMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TransitionResult2TwinMode implements MapperModePointer<TwinMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinMode point() {
            return switch (this) {
                case HIDE -> TwinMode.HIDE;
                case SHORT -> TwinMode.SHORT;
                case DETAILED -> TwinMode.DETAILED;
            };
        }
    }
}
