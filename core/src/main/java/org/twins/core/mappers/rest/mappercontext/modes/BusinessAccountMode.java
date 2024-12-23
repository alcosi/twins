package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum BusinessAccountMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum BusinessAccountUser2BusinessAccountMode implements MapperModePointer<BusinessAccountMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public BusinessAccountMode point() {
            return switch (this) {
                case HIDE -> BusinessAccountMode.HIDE;
                case SHORT -> BusinessAccountMode.SHORT;
                case DETAILED -> BusinessAccountMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum DomainBusinessAccount2BusinessAccountMode implements MapperModePointer<BusinessAccountMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public BusinessAccountMode point() {
            return switch (this) {
                case HIDE -> BusinessAccountMode.HIDE;
                case SHORT -> BusinessAccountMode.SHORT;
                case DETAILED -> BusinessAccountMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum DomainUser2BusinessAccountMode implements MapperModePointer<BusinessAccountMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public BusinessAccountMode point() {
            return switch (this) {
                case HIDE -> BusinessAccountMode.HIDE;
                case SHORT -> BusinessAccountMode.SHORT;
                case DETAILED -> BusinessAccountMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum PermissionSchema2BusinessAccountMode implements MapperModePointer<BusinessAccountMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public BusinessAccountMode point() {
            return switch (this) {
                case HIDE -> BusinessAccountMode.HIDE;
                case SHORT -> BusinessAccountMode.SHORT;
                case DETAILED -> BusinessAccountMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinflowSchema2BusinessAccountMode implements MapperModePointer<BusinessAccountMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public BusinessAccountMode point() {
            return switch (this) {
                case HIDE -> BusinessAccountMode.HIDE;
                case SHORT -> BusinessAccountMode.SHORT;
                case DETAILED -> BusinessAccountMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum SpaceRole2BusinessAccountMode implements MapperModePointer<BusinessAccountMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public BusinessAccountMode point() {
            return switch (this) {
                case HIDE -> BusinessAccountMode.HIDE;
                case SHORT -> BusinessAccountMode.SHORT;
                case DETAILED -> BusinessAccountMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum UserGroup2BusinessAccountMode implements MapperModePointer<BusinessAccountMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public BusinessAccountMode point() {
            return switch (this) {
                case HIDE -> BusinessAccountMode.HIDE;
                case SHORT -> BusinessAccountMode.SHORT;
                case DETAILED -> BusinessAccountMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum DataListOption2BusinessAccountMode implements MapperModePointer<BusinessAccountMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public BusinessAccountMode point() {
            return switch (this) {
                case HIDE -> BusinessAccountMode.HIDE;
                case SHORT -> BusinessAccountMode.SHORT;
                case DETAILED -> BusinessAccountMode.DETAILED;
            };
        }
    }
}
