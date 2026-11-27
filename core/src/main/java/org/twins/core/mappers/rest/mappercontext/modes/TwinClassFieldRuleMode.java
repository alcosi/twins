package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;
import org.twins.core.service.permission.Permissions;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum TwinClassFieldRuleMode implements MapperMode {
    @FieldNameConstants.Include HIDE(0),
    @FieldNameConstants.Include SHORT(1),
    @FieldNameConstants.Include DETAILED(2),
    @ProtectedBy(Permissions.TWIN_CLASS_FIELD_MANAGE)
    @FieldNameConstants.Include MANAGED(3);

    final int priority;

    @Getter
    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinClassField2TwinClassFieldRuleMode implements MapperModePointer<TwinClassFieldRuleMode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2),
        @FieldNameConstants.Include MANAGED(3);

        final int priority;

        @Override
        public TwinClassFieldRuleMode point() {
            return switch (this) {
                case HIDE -> TwinClassFieldRuleMode.HIDE;
                case SHORT -> TwinClassFieldRuleMode.SHORT;
                case DETAILED -> TwinClassFieldRuleMode.DETAILED;
                case MANAGED -> TwinClassFieldRuleMode.MANAGED;
            };
        }
    }
}
