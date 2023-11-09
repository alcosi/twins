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
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserService;

import java.util.Properties;

@Component
@Featurer(id = 1102,
        name = "BusinessAccountInitiatorFromParams",
        description = "")
public class BusinessAccountInitiatorFromParamsPostCreate extends BusinessAccountInitiatorFromParams {
    @FeaturerParam(name = "businessAccountTemplateTwinId", description = "")
    public static final FeaturerParamUUID businessAccountTemplateTwinId = new FeaturerParamUUID("businessAccountTemplateTwinId");

    @Lazy
    @Autowired
    TwinService twinService;
    @Lazy
    @Autowired
    AuthService authService;

    @Override
    protected void postInit(Properties properties, DomainBusinessAccountEntity domainBusinessAccountEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        twinService.duplicateTwin(businessAccountTemplateTwinId.extract(properties), domainBusinessAccountEntity.getBusinessAccount(), apiUser.getUser(), domainBusinessAccountEntity.getId()); //creating twin for business account in domain
    }
}
