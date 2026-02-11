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
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Properties;


@Component
@Featurer(id = FeaturerTwins.ID_2330,
        name = "Field user from output twin head assignee",
        description = "Fill the user field with assignee own head twin assignee")
@Slf4j
public class FillerFieldUserFromOutputTwinHeadAssignee extends Filler {

    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    private TwinClassFieldService twinClassFieldService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity factoryItemTwin = factoryItem.getTwin();
        TwinEntity headTwin = twinService.loadHeadForTwin(factoryItemTwin);
        if(null == headTwin)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No head twin detected for twin: " + factoryItemTwin.logDetailed());
        if(null == headTwin.getAssignerUserId())
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No assignee for head[" + headTwin.logShort() + "]twin detected for twin: " + factoryItemTwin.logDetailed());
        FieldValueUser fieldValue = new FieldValueUser(twinClassFieldService.findEntitySafe(twinClassFieldId.extract(properties)));
        fieldValue.add(headTwin.getAssignerUser());
        factoryItem.getOutput().addField(fieldValue);
    }
}
