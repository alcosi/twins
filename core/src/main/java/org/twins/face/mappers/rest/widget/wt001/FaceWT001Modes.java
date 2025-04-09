package org.twins.face.mappers.rest.widget.wt001;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldMode;

public class FaceWT001Modes {
    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FaceWT001AccordionItemCollectionMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHOW(1);
        final int priority;
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FaceWT001Column2TwinClassFieldMode implements MapperModePointer<TwinClassFieldMode> {
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
