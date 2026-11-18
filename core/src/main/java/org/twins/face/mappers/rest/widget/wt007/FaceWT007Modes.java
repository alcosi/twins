package org.twins.face.mappers.rest.widget.wt007;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;

public class FaceWT007Modes {
    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FaceWT007Button2TransitionMode implements MapperModePointer<TransitionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);
        final int priority;

        @Override
        public TransitionMode point() {
            return switch (this) {
                case HIDE -> TransitionMode.HIDE;
                case SHORT -> TransitionMode.SHORT;
                case DETAILED -> TransitionMode.DETAILED;
                case MANAGED -> TransitionMode.MANAGED;
            };
        }
    }
}
