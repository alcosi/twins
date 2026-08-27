package org.twins.core.featurer.notificator.notifier;

import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;
import java.util.Set;

@Component
@Featurer(id = FeaturerTwins.ID_4802,
        name = "Stub notifier",
        description = "")
public class NotifierStub extends Notifier {
    @Override
    protected Set<NotifyEvent> notify(Properties properties, Set<NotifyEvent> notifyEvents) {
        return Set.of(); // nothing to do — everything "sent"
    }
}
