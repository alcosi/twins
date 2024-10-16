package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum BusinessAccountUserCollectionMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHOW(1);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum DomainUser2BusinessAccountUserCollectionMode implements MapperModePointer<BusinessAccountUserCollectionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHOW(1);

        final int priority;

        @Override
        public BusinessAccountUserCollectionMode point() {
            return switch (this) {
                case HIDE -> BusinessAccountUserCollectionMode.HIDE;
                case SHOW -> BusinessAccountUserCollectionMode.SHOW;
            };
        }
    }
}
