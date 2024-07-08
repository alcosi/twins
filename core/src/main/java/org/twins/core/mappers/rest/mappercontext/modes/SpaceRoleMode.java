package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum SpaceRoleMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum SpaceRoleUser2SpaceRoleMode implements MapperModePointer<SpaceRoleMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public SpaceRoleMode point() {
            return switch (this) {
                case HIDE -> SpaceRoleMode.HIDE;
                case SHORT -> SpaceRoleMode.SHORT;
                case DETAILED -> SpaceRoleMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum SpaceRoleUserGroup2SpaceRoleMode implements MapperModePointer<SpaceRoleMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public SpaceRoleMode point() {
            return switch (this) {
                case HIDE -> SpaceRoleMode.HIDE;
                case SHORT -> SpaceRoleMode.SHORT;
                case DETAILED -> SpaceRoleMode.DETAILED;
            };
        }
    }
}
