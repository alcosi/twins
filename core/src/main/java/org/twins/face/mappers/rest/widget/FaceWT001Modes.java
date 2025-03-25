package org.twins.face.mappers.rest.widget;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;

public class FaceWT001Modes {
    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FaceWT001AccordionItemCollectionMode implements MapperMode {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHOW(1);
        final int priority;
    }
}
