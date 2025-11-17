package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum AttachmentMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Comment2AttachmentMode implements MapperModePointer<AttachmentMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public AttachmentMode point() {
            return switch (this) {
                case HIDE -> AttachmentMode.HIDE;
                case SHORT -> AttachmentMode.SHORT;
                case DETAILED -> AttachmentMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Twin2AttachmentMode implements MapperModePointer<AttachmentMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public AttachmentMode point() {
            return switch (this) {
                case HIDE -> AttachmentMode.HIDE;
                case SHORT -> AttachmentMode.SHORT;
                case DETAILED -> AttachmentMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinField2AttachmentMode implements MapperModePointer<AttachmentMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public AttachmentMode point() {
            return switch (this) {
                case HIDE -> AttachmentMode.HIDE;
                case SHORT -> AttachmentMode.SHORT;
                case DETAILED -> AttachmentMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum AttachmentCUDValidate2AttachmentMode implements MapperModePointer<AttachmentMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public AttachmentMode point() {
            return switch (this) {
                case HIDE -> AttachmentMode.HIDE;
                case SHORT -> AttachmentMode.SHORT;
                case DETAILED -> AttachmentMode.DETAILED;
            };
        }
    }
}
