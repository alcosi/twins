package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum FaceMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum DomainNavbar2FaceMode implements MapperModePointer<FaceMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FaceMode point() {
            return switch (this) {
                case HIDE -> FaceMode.HIDE;
                case SHORT -> FaceMode.SHORT;
                case DETAILED -> FaceMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinClassPage2FaceMode implements MapperModePointer<FaceMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FaceMode point() {
            return switch (this) {
                case HIDE -> FaceMode.HIDE;
                case SHORT -> FaceMode.SHORT;
                case DETAILED -> FaceMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Twin2FaceMode implements MapperModePointer<FaceMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FaceMode point() {
            return switch (this) {
                case HIDE -> FaceMode.HIDE;
                case SHORT -> FaceMode.SHORT;
                case DETAILED -> FaceMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum ModalFace2FaceMode implements MapperModePointer<FaceMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public FaceMode point() {
            return switch (this) {
                case HIDE -> FaceMode.HIDE;
                case SHORT -> FaceMode.SHORT;
                case DETAILED -> FaceMode.DETAILED;
            };
        }
    }
}
