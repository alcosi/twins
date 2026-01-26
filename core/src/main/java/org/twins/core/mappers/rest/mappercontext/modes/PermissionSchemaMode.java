package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum PermissionSchemaMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum PermissionGrantUser2PermissionSchemaMode implements MapperModePointer<PermissionSchemaMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public PermissionSchemaMode point() {
            return switch (this) {
                case HIDE -> PermissionSchemaMode.HIDE;
                case SHORT -> PermissionSchemaMode.SHORT;
                case DETAILED -> PermissionSchemaMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum PermissionGrantUserGroup2PermissionSchemaMode implements MapperModePointer<PermissionSchemaMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public PermissionSchemaMode point() {
            return switch (this) {
                case HIDE -> PermissionSchemaMode.HIDE;
                case SHORT -> PermissionSchemaMode.SHORT;
                case DETAILED -> PermissionSchemaMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum PermissionGrantSpaceRole2PermissionSchemaMode implements MapperModePointer<PermissionSchemaMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public PermissionSchemaMode point() {
            return switch (this) {
                case HIDE -> PermissionSchemaMode.HIDE;
                case SHORT -> PermissionSchemaMode.SHORT;
                case DETAILED -> PermissionSchemaMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum PermissionGrantAssigneePropagation2PermissionSchemaMode implements MapperModePointer<PermissionSchemaMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public PermissionSchemaMode point() {
            return switch (this) {
                case HIDE -> PermissionSchemaMode.HIDE;
                case SHORT -> PermissionSchemaMode.SHORT;
                case DETAILED -> PermissionSchemaMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum PermissionGrantTwinRole2PermissionSchemaMode implements MapperModePointer<PermissionSchemaMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public PermissionSchemaMode point() {
            return switch (this) {
                case HIDE -> PermissionSchemaMode.HIDE;
                case SHORT -> PermissionSchemaMode.SHORT;
                case DETAILED -> PermissionSchemaMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Tier2PermissionSchemaMode implements MapperModePointer<PermissionSchemaMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public PermissionSchemaMode point() {
            return switch (this) {
                case HIDE -> PermissionSchemaMode.HIDE;
                case SHORT -> PermissionSchemaMode.SHORT;
                case DETAILED -> PermissionSchemaMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Domain2PermissionSchemaMode implements MapperModePointer<PermissionSchemaMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public PermissionSchemaMode point() {
            return switch (this) {
                case HIDE -> PermissionSchemaMode.HIDE;
                case SHORT -> PermissionSchemaMode.SHORT;
                case DETAILED -> PermissionSchemaMode.DETAILED;
            };
        }
    }
}

