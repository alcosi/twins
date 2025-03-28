package org.twins.face.mappers.rest.navbar.nb001;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;

public class FaceNB001Modes {
    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FaceNB001MenuItemCollectionMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHOW(1);
        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FaceNB001MenuItem2FaceMode implements MapperModePointer<FaceMode> {
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
