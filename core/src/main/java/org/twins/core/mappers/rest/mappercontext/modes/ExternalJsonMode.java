package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum ExternalJsonMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHOW(1);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum ExternalJson2TwinClassMode implements MapperModePointer<ExternalJsonMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHOW(1);

        final int priority;

        @Override
        public ExternalJsonMode point() {
            return switch (this) {
                case HIDE -> ExternalJsonMode.HIDE;
                case SHOW -> ExternalJsonMode.SHOW;
            };
        }
    }

}
