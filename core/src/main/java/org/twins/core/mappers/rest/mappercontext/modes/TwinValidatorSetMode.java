package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum TwinValidatorSetMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinValidator2TwinValidatorSetMode implements MapperModePointer<TwinValidatorSetMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinValidatorSetMode point() {
            return switch (this) {
                case HIDE -> TwinValidatorSetMode.HIDE;
                case SHORT -> TwinValidatorSetMode.SHORT;
                case DETAILED -> TwinValidatorSetMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinActionValidatorRule2TwinValidatorSetMode implements MapperModePointer<TwinValidatorSetMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinValidatorSetMode point() {
            return switch (this) {
                case HIDE -> TwinValidatorSetMode.HIDE;
                case SHORT -> TwinValidatorSetMode.SHORT;
                case DETAILED -> TwinValidatorSetMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinflowTransitionValidatorRule2TwinValidatorSetMode implements MapperModePointer<TwinValidatorSetMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinValidatorSetMode point() {
            return switch (this) {
                case HIDE -> TwinValidatorSetMode.HIDE;
                case SHORT -> TwinValidatorSetMode.SHORT;
                case DETAILED -> TwinValidatorSetMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinCommentActionAlienValidatorRule2TwinValidatorSetMode implements MapperModePointer<TwinValidatorSetMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinValidatorSetMode point() {
            return switch (this) {
                case HIDE -> TwinValidatorSetMode.HIDE;
                case SHORT -> TwinValidatorSetMode.SHORT;
                case DETAILED -> TwinValidatorSetMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum HistoryNotificationSchemaMap2TwinValidatorSetMode implements MapperModePointer<TwinValidatorSetMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinValidatorSetMode point() {
            return switch (this) {
                case HIDE -> TwinValidatorSetMode.HIDE;
                case SHORT -> TwinValidatorSetMode.SHORT;
                case DETAILED -> TwinValidatorSetMode.DETAILED;
            };
        }
    }


}
