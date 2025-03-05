package org.twins.core.featurer.transition.trigger;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;

import java.util.Properties;

@Slf4j
public abstract class TransitionTriggerRabbitMqConnection extends TransitionTrigger {

    @FeaturerParam(name = "url", description = "rabbit server url", optional = false)
    public static final FeaturerParamString url = new FeaturerParamString("url");

    @FeaturerParam(name = "port", description = "rabbit server port", optional = false)
    public static final FeaturerParamInt port = new FeaturerParamInt("port");

    //todo parameters etc.


    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        Object connection = connect(properties);
        send(properties, twinEntity, srcTwinStatus, dstTwinStatus);
    }
    /**
     * TODO main question. Connect to rmq - is a hard operation? We want to use possibility to establish
     * TODO connection to specific server(from trigger parameters). Not one queue for all application.
     * TODO May be connections can be stored in cache? and delete from cache in 1h period.
     **/

    public Object connect(Properties properties) {
        Integer portValue = port.extract(properties);
        //... todo set connection
        return null;
    }

    public abstract void send(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus);

}
