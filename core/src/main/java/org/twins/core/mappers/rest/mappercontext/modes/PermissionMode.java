package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum PermissionMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Transition2PermissionMode implements MapperModePointer<PermissionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public PermissionMode point() {
            return switch (this) {
                case HIDE -> PermissionMode.HIDE;
                case SHORT -> PermissionMode.SHORT;
                case DETAILED -> PermissionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinClass2PermissionMode implements MapperModePointer<PermissionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public PermissionMode point() {
            return switch (this) {
                case HIDE -> PermissionMode.HIDE;
                case SHORT -> PermissionMode.SHORT;
                case DETAILED -> PermissionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum PermissionGrantUserGroup2PermissionMode implements MapperModePointer<PermissionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public PermissionMode point() {
            return switch (this) {
                case HIDE -> PermissionMode.HIDE;
                case SHORT -> PermissionMode.SHORT;
                case DETAILED -> PermissionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum PermissionGrantUser2PermissionMode implements MapperModePointer<PermissionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public PermissionMode point() {
            return switch (this) {
                case HIDE -> PermissionMode.HIDE;
                case SHORT -> PermissionMode.SHORT;
                case DETAILED -> PermissionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum PermissionGrantTwinRole2PermissionMode implements MapperModePointer<PermissionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public PermissionMode point() {
            return switch (this) {
                case HIDE -> PermissionMode.HIDE;
                case SHORT -> PermissionMode.SHORT;
                case DETAILED -> PermissionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum PermissionGrantSpaceRole2PermissionMode implements MapperModePointer<PermissionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public PermissionMode point() {
            return switch (this) {
                case HIDE -> PermissionMode.HIDE;
                case SHORT -> PermissionMode.SHORT;
                case DETAILED -> PermissionMode.DETAILED;
            };
        }
    }


    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinClassField2PermissionMode implements MapperModePointer<PermissionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public PermissionMode point() {
            return switch (this) {
                case HIDE -> PermissionMode.HIDE;
                case SHORT -> PermissionMode.SHORT;
                case DETAILED -> PermissionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Attachment2PermissionMode implements MapperModePointer<PermissionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public PermissionMode point() {
            return switch (this) {
                case HIDE -> PermissionMode.HIDE;
                case SHORT -> PermissionMode.SHORT;
                case DETAILED -> PermissionMode.DETAILED;
            };
        }
    }
}
