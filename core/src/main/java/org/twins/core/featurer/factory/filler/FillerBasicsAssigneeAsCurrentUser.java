package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.auth.AuthService;

import java.util.Collection;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2362,
        name = "Basics assignee as current user",
        description = "")
@Slf4j
public class FillerBasicsAssigneeAsCurrentUser extends Filler {
    @Autowired
    AuthService authService;

    @Override
    public void fill(Properties properties, Collection<FactoryItem> factoryItems, TwinEntity templateTwin, boolean optional) throws ServiceException {
        fillEach(properties, factoryItems, templateTwin, optional);
    }

    @Override
    protected void fillItem(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        var currentUser = authService.getApiUser().getUser();
        outputTwinEntity
                .setAssignerUser(currentUser)
                .setAssignerUserId(currentUser.getId());
    }
}
