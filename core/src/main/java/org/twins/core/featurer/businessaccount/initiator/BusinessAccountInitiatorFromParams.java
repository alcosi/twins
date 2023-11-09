package org.twins.core.featurer.businessaccount.initiator;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = 1101,
        name = "BusinessAccountInitiatorFromParams",
        description = "")
@RequiredArgsConstructor
public class BusinessAccountInitiatorFromParams extends BusinessAccountInitiator {
    @FeaturerParam(name = "permissionSchemaId", description = "")
    public static final FeaturerParamUUID permissionSchemaId = new FeaturerParamUUID("permissionSchemaId");
    @FeaturerParam(name = "twinClassSchemaId", description = "")
    public static final FeaturerParamUUID twinClassSchemaId = new FeaturerParamUUID("twinClassSchemaId");
    @FeaturerParam(name = "twinflowSchemaId", description = "")
    public static final FeaturerParamUUID twinflowSchemaId = new FeaturerParamUUID("twinflowSchemaId");

    @Override
    protected void init(Properties properties, DomainBusinessAccountEntity domainBusinessAccountEntity) throws ServiceException {
        domainBusinessAccountEntity
                .setPermissionSchemaId(permissionSchemaId.extract(properties))
                .setTwinClassSchemaId(twinClassSchemaId.extract(properties))
                .setTwinflowSchemaId(twinflowSchemaId.extract(properties));
    }
}
