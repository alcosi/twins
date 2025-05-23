package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum TwinClassSchemaMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Tier2TwinClassSchemaMode implements MapperModePointer<TwinClassSchemaMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinClassSchemaMode point() {
            return switch (this) {
                case HIDE -> TwinClassSchemaMode.HIDE;
                case SHORT -> TwinClassSchemaMode.SHORT;
                case DETAILED -> TwinClassSchemaMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum Domain2TwinClassSchemaMode implements MapperModePointer<TwinClassSchemaMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinClassSchemaMode point() {
            return switch (this) {
                case HIDE -> TwinClassSchemaMode.HIDE;
                case SHORT -> TwinClassSchemaMode.SHORT;
                case DETAILED -> TwinClassSchemaMode.DETAILED;
            };
        }
    }
}
