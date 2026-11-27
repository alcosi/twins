package org.twins.core.mappers.rest.mappercontext.modes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;
import org.twins.core.service.permission.Permissions;

@Getter
@AllArgsConstructor
@FieldNameConstants(onlyExplicitlyIncluded = true)
public enum FactoryUsagesMode implements MapperModePointer<UsagesMode> {
    @FieldNameConstants.Include HIDE(0),
    @ProtectedBy({Permissions.FACTORY_MANAGE, Permissions.FACTORY_UPDATE})
    @FieldNameConstants.Include SHORT(1),
    @ProtectedBy({Permissions.FACTORY_MANAGE, Permissions.FACTORY_UPDATE})
    @FieldNameConstants.Include DETAILED(2);

    final int priority;

    @Override
    public UsagesMode point() {
        return switch (this) {
            case HIDE -> UsagesMode.HIDE;
            case SHORT -> UsagesMode.SHORT;
            case DETAILED -> UsagesMode.DETAILED;
        };
    }
}
