package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum UserGroupMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum PermissionGrantUserGroup2UserGroupMode implements MapperModePointer<UserGroupMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserGroupMode point() {
            return switch (this) {
                case HIDE -> UserGroupMode.HIDE;
                case SHORT -> UserGroupMode.SHORT;
                case DETAILED -> UserGroupMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum UserGroupByAssigneePropagation2UserGroupMode implements MapperModePointer<UserGroupMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserGroupMode point() {
            return switch (this) {
                case HIDE -> UserGroupMode.HIDE;
                case SHORT -> UserGroupMode.SHORT;
                case DETAILED -> UserGroupMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum User2UserGroupMode implements MapperModePointer<UserGroupMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public UserGroupMode point() {
            return switch (this) {
                case HIDE -> UserGroupMode.HIDE;
                case SHORT -> UserGroupMode.SHORT;
                case DETAILED -> UserGroupMode.DETAILED;
            };
        }
    }
}
