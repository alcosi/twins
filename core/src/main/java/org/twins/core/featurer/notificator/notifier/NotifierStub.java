package org.twins.core.featurer.notificator.notifier;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_4802,
        name = "Stub notifier",
        description = "")
public class NotifierStub extends Notifier {
    @Override
    protected void notify(Set<UUID> recipientIds, Map<String, String> context, String eventCode, Properties properties) throws ServiceException {
        //do nothing
    }
}
