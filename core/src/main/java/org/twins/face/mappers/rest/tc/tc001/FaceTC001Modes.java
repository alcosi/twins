package org.twins.face.mappers.rest.tc.tc001;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;

public class FaceTC001Modes {
    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FaceTC0012TwinClassFieldMode implements MapperModePointer<TwinClassFieldMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);

        final int priority;

        @Override
        public TwinClassFieldMode point() {
            return switch (this) {
                case HIDE -> TwinClassFieldMode.HIDE;
                case SHORT -> TwinClassFieldMode.SHORT;
                case DETAILED -> TwinClassFieldMode.DETAILED;
                case MANAGED -> TwinClassFieldMode.MANAGED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FaceTC0012TwinClassMode implements MapperModePointer<TwinClassMode> {
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
