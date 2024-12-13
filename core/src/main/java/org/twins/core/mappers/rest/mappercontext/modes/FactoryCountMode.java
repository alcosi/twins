package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum FactoryCountMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHOW(1);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FactoryUsagesCountMode implements MapperModePointer<FactoryCountMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHOW(1);

        final int priority;

        @Override
        public FactoryCountMode point() {
            return switch (this) {
                case HIDE -> FactoryCountMode.HIDE;
                case SHOW -> FactoryCountMode.SHOW;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FactoryPipelinesCountMode implements MapperModePointer<FactoryCountMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHOW(1);

        final int priority;

        @Override
        public FactoryCountMode point() {
            return switch (this) {
                case HIDE -> FactoryCountMode.HIDE;
                case SHOW -> FactoryCountMode.SHOW;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FactoryMultipliersCountMode implements MapperModePointer<FactoryCountMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHOW(1);

        final int priority;

        @Override
        public FactoryCountMode point() {
            return switch (this) {
                case HIDE -> FactoryCountMode.HIDE;
                case SHOW -> FactoryCountMode.SHOW;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FactoryBranchesCountMode implements MapperModePointer<FactoryCountMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHOW(1);

        final int priority;

        @Override
        public FactoryCountMode point() {
            return switch (this) {
                case HIDE -> FactoryCountMode.HIDE;
                case SHOW -> FactoryCountMode.SHOW;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum FactoryErasersCountMode implements MapperModePointer<FactoryCountMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHOW(1);

        final int priority;

        @Override
        public FactoryCountMode point() {
            return switch (this) {
                case HIDE -> FactoryCountMode.HIDE;
                case SHOW -> FactoryCountMode.SHOW;
            };
        }
    }
}
