package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2326,
        name = "Basics assignee from context twin of class assignee",
        description = "")
@Slf4j
public class FillerBasicsAssigneeFromContextTwinOfClassAssignee extends FillerAtomic {
    @FeaturerParam(name = "Twin class id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassIdParam = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");

    @Autowired
    TwinService twinService;

    @Override
    protected void beforeFill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin) throws ServiceException {
        var contextTwins = batch.getContextTwins(twinClassIdParam.extract(properties));
        if (CollectionUtils.isNotEmpty(contextTwins)) {
            twinService.loadUser(contextTwins);
        }
    }

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        UUID twinClassId = twinClassIdParam.extract(properties);
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        TwinEntity contextTwin = factoryItem.lookupTwinOfClass(twinClassId);
        if (null != contextTwin)
        outputTwinEntity
                .setAssignerUser(contextTwin.getAssignerUser())
                .setAssignerUserId(contextTwin.getAssignerUserId());
        else throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Twin of TwinClass[" + twinClassId + "] is not present in context");
    }
}
