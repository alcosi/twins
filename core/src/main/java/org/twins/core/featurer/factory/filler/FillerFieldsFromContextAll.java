package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;

@Component
@Featurer(id = FeaturerTwins.ID_2310,
        name = "FieldsFromContextAll",
        description = "")
@Slf4j
public class FillerFieldsFromContextAll extends Filler {
    @Lazy
    @Autowired
    TwinClassService twinClassService;

    @Lazy
    @Autowired
    TwinService twinService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        Map<UUID, FieldValue> contextFields = factoryItem.getFactoryContext().getFields();
        if (contextFields == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No context fields present. Please check pipeline config");
        List<String> logMsgs = new ArrayList<>();
        for (Map.Entry<UUID, FieldValue> fieldValue : contextFields.entrySet()) {
            if (twinClassService.isInstanceOf(outputTwinEntity.getTwinClass(), fieldValue.getValue().getTwinClassField().getTwinClassId())) {
                logMsgs.add(outputTwinEntity.logShort() + " " + fieldValue.getValue().getTwinClassField().logNormal() + " will be filled from context");
                factoryItem.getOutput().addField(fieldValue.getValue().clone());
            }
        }
        if (!logMsgs.isEmpty())
            log.info(String.join(System.lineSeparator(), logMsgs));
    }
}
