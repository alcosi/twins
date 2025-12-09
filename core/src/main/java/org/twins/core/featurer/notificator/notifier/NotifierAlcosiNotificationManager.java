package org.twins.core.featurer.notificator.notifier;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4801,
        name = "Notifier Alcosi Notification Manager",
        description = "")
public class NotifierAlcosiNotificationManager extends Notifier {

    @Override
    protected void notify(HistoryEntity history, Properties properties) throws ServiceException {
        //todo impl me
        //что то писать в Rabbit, что может прочитать сервис Игоря
    }
}
