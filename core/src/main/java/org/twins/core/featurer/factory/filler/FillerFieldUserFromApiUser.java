package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2338,
        name = "Basics assignee from api user",
        description = "")
@Slf4j
public class FillerFieldUserFromApiUser extends Filler {

    @FeaturerParam(name = "Twin class field id", description = "TwinClassFieldId for filling")
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @Lazy
    @Autowired
    private AuthService authService;

    @Lazy
    @Autowired
    private TwinClassFieldService twinClassFieldService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        FieldValueUser fieldValue = new FieldValueUser(twinClassFieldService.findEntitySafe(twinClassFieldId.extract(properties)));
        fieldValue.add(apiUser.getUser());
        factoryItem.getOutput().addField(fieldValue);
    }
}
