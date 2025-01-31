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
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.service.factory.TwinFactoryService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2326,
        name = "Basics assignee from context twin of class assignee",
        description = "")
@Slf4j
public class FillerBasicsAssigneeFromContextTwinOfClassAssignee extends Filler {
    @FeaturerParam(name = "Twin class id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");

    @Lazy
    @Autowired
    TwinFactoryService twinFactoryService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        UUID id = twinClassId.extract(properties);
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        TwinEntity contextTwin = twinFactoryService.lookupTwinOfClass(factoryItem, id, 0);
        if(null != contextTwin)
        outputTwinEntity
                .setAssignerUser(contextTwin.getAssignerUser())
                .setAssignerUserId(contextTwin.getAssignerUserId());
        else throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Twin of TwinClass[" + id + "] is not present in context");
    }
}
