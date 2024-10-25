package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum TwinValidatorMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;


    //todo maybe in future can be splitted into not abstract pointer modes(see ValidatorRuleMode.class)

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum ValidatorRule2TwinValidatorMode implements MapperModePointer<TwinValidatorMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinValidatorMode point() {
            return switch (this) {
                case HIDE -> TwinValidatorMode.HIDE;
                case SHORT -> TwinValidatorMode.SHORT;
                case DETAILED -> TwinValidatorMode.DETAILED;
            };
        }
    }
}
