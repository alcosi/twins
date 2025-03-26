package org.twins.face.mappers.rest.twidget;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldMode;

public class FaceTW002Modes {
    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FaceTW0022TwinClassFieldMode implements MapperModePointer<TwinClassFieldMode> {
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
}
