package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum ValidatorRuleMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinflowTransitionValidatorRule2ValidatorRuleMode implements MapperModePointer<ValidatorRuleMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public ValidatorRuleMode point() {
            return switch (this) {
                case HIDE -> ValidatorRuleMode.HIDE;
                case SHORT -> ValidatorRuleMode.SHORT;
                case DETAILED -> ValidatorRuleMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinActionValidatorRule2ValidatorRuleMode implements MapperModePointer<ValidatorRuleMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public ValidatorRuleMode point() {
            return switch (this) {
                case HIDE -> ValidatorRuleMode.HIDE;
                case SHORT -> ValidatorRuleMode.SHORT;
                case DETAILED -> ValidatorRuleMode.DETAILED;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum  TwinCommentActionAlienValidatorRule2ValidatorRuleMode implements MapperModePointer<ValidatorRuleMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        final int priority;

        @Override
        public ValidatorRuleMode point() {
            return switch (this) {
                case HIDE -> ValidatorRuleMode.HIDE;
                case SHORT -> ValidatorRuleMode.SHORT;
                case DETAILED -> ValidatorRuleMode.DETAILED;
            };
        }
    }
}
