package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum DataListOptionMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinClassFieldDescriptorOnDataListOptionMode implements MapperModePointer<DataListOptionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public DataListOptionMode point() {
            return switch (this) {
                case HIDE -> DataListOptionMode.HIDE;
                case SHORT -> DataListOptionMode.SHORT;
                case DETAILED -> DataListOptionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinClassMarkerModeOnDataListOptionMode implements MapperModePointer<DataListOptionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public DataListOptionMode point() {
            return switch (this) {
                case HIDE -> DataListOptionMode.HIDE;
                case SHORT -> DataListOptionMode.SHORT;
                case DETAILED -> DataListOptionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinClassTagOnDataListOptionMode implements MapperModePointer<DataListOptionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public DataListOptionMode point() {
            return switch (this) {
                case HIDE -> DataListOptionMode.HIDE;
                case SHORT -> DataListOptionMode.SHORT;
                case DETAILED -> DataListOptionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinFieldOnDataListOptionMode implements MapperModePointer<DataListOptionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public DataListOptionMode point() {
            return switch (this) {
                case HIDE -> DataListOptionMode.HIDE;
                case SHORT -> DataListOptionMode.SHORT;
                case DETAILED -> DataListOptionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinMarkerOnDataListOptionMode implements MapperModePointer<DataListOptionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public DataListOptionMode point() {
            return switch (this) {
                case HIDE -> DataListOptionMode.HIDE;
                case SHORT -> DataListOptionMode.SHORT;
                case DETAILED -> DataListOptionMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinTagOnDataListOptionMode implements MapperModePointer<DataListOptionMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public DataListOptionMode point() {
            return switch (this) {
                case HIDE -> DataListOptionMode.HIDE;
                case SHORT -> DataListOptionMode.SHORT;
                case DETAILED -> DataListOptionMode.DETAILED;
            };
        }
    }
}
