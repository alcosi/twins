package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum TwinflowTransitionValidatorRuleMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinflowTransition2TwinflowTransitionValidatorRuleMode implements MapperModePointer<TwinflowTransitionValidatorRuleMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public TwinflowTransitionValidatorRuleMode point() {
            return switch (this) {
                case HIDE -> TwinflowTransitionValidatorRuleMode.HIDE;
                case SHORT -> TwinflowTransitionValidatorRuleMode.SHORT;
                case DETAILED -> TwinflowTransitionValidatorRuleMode.DETAILED;
            };
        }
    }
}