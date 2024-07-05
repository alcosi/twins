package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum UserMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum AttachmentOnUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum CommentOnUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinClassFieldDescriptorOnUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum HistoryOnUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum SpaceOnUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TransitionOnUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinClassOnUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinLinkOnUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinflowOnUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinOnUserMode implements MapperModePointer<UserMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserMode point() {
            return switch (this) {
                case HIDE -> UserMode.HIDE;
                case SHORT -> UserMode.SHORT;
                case DETAILED -> UserMode.DETAILED;
            };
        }
    }
}
