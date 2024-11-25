package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum TwinClassMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2),
    @FieldNameConstants.Include MANAGED(3);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public
    enum Twinflow2TwinClassMode implements MapperModePointer<TwinClassMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);

        final int priority;

        @Override
        public TwinClassMode point() {
            return switch (this) {
                case HIDE -> TwinClassMode.HIDE;
                case SHORT -> TwinClassMode.SHORT;
                case DETAILED -> TwinClassMode.DETAILED;
                case MANAGED -> TwinClassMode.MANAGED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public
    enum TwinClassExtends2TwinClassMode implements MapperModePointer<TwinClassMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);

        final int priority;

        @Override
        public TwinClassMode point() {
            return switch (this) {
                case HIDE -> TwinClassMode.HIDE;
                case SHORT -> TwinClassMode.SHORT;
                case DETAILED -> TwinClassMode.DETAILED;
                case MANAGED -> TwinClassMode.MANAGED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public
    enum TwinClassHead2TwinClassMode implements MapperModePointer<TwinClassMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);

        final int priority;

        @Override
        public TwinClassMode point() {
            return switch (this) {
                case HIDE -> TwinClassMode.HIDE;
                case SHORT -> TwinClassMode.SHORT;
                case DETAILED -> TwinClassMode.DETAILED;
                case MANAGED -> TwinClassMode.MANAGED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public
    enum LinkDst2TwinClassMode implements MapperModePointer<TwinClassMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);

        final int priority;

        @Override
        public TwinClassMode point() {
            return switch (this) {
                case HIDE -> TwinClassMode.HIDE;
                case SHORT -> TwinClassMode.SHORT;
                case DETAILED -> TwinClassMode.DETAILED;
                case MANAGED -> TwinClassMode.MANAGED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public
    enum LinkSrc2TwinClassMode implements MapperModePointer<TwinClassMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);

        final int priority;

        @Override
        public TwinClassMode point() {
            return switch (this) {
                case HIDE -> TwinClassMode.HIDE;
                case SHORT -> TwinClassMode.SHORT;
                case DETAILED -> TwinClassMode.DETAILED;
                case MANAGED -> TwinClassMode.MANAGED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public
    enum Twin2TwinClassMode implements MapperModePointer<TwinClassMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);

        final int priority;

        @Override
        public TwinClassMode point() {
            return switch (this) {
                case HIDE -> TwinClassMode.HIDE;
                case SHORT -> TwinClassMode.SHORT;
                case DETAILED -> TwinClassMode.DETAILED;
                case MANAGED -> TwinClassMode.MANAGED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public
    enum PermissionGroup2TwinClassMode implements MapperModePointer<TwinClassMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);

        final int priority;

        @Override
        public TwinClassMode point() {
            return switch (this) {
                case HIDE -> TwinClassMode.HIDE;
                case SHORT -> TwinClassMode.SHORT;
                case DETAILED -> TwinClassMode.DETAILED;
                case MANAGED -> TwinClassMode.MANAGED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public
    enum SpaceRole2TwinClassMode implements MapperModePointer<TwinClassMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);

        final int priority;

        @Override
        public TwinClassMode point() {
            return switch (this) {
                case HIDE -> TwinClassMode.HIDE;
                case SHORT -> TwinClassMode.SHORT;
                case DETAILED -> TwinClassMode.DETAILED;
                case MANAGED -> TwinClassMode.MANAGED;
            };
        }
    }
}
