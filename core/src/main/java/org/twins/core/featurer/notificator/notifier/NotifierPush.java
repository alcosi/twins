package org.twins.core.featurer.notificator.notifier;

import org.aspectj.weaver.ast.Not;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.notificator.recipient.RecipientResolver;
import org.twins.core.featurer.params.FeaturerParamUUIDSetUserId;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4801,
        name = "Notifier push",
        description = "")
public class NotifierPush extends Notifier {

    @Override
    protected void notify(HistoryEntity history, Properties properties) throws ServiceException {
        //todo impl me
    }
}
