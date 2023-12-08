package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapperV2;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;

@Component
@Featurer(id = 2310,
        name = "FillerFieldsFromContextAll",
        description = "")
@Slf4j
public class FillerFieldsFromContextAll extends Filler {
    @Lazy
    @Autowired
    TwinClassService twinClassService;

    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    TwinFieldRestDTOMapperV2 twinFieldRestDTOMapperV2;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutputTwin().getTwinEntity();
        Map<UUID, FieldValue> contextFields = factoryItem.getFactoryContext().getFields();
        if (contextFields == null) {
            log.error("No context fields present. Please check pipeline config");
            return;
        }
        List<String> logMsgs = new ArrayList<>();
        for (Map.Entry<UUID, FieldValue> fieldValue : contextFields.entrySet()) {
            if (twinClassService.isInstanceOf(outputTwinEntity.getTwinClass(), fieldValue.getValue().getTwinClassField().getTwinClassId())) {
                logMsgs.add(outputTwinEntity + "" + fieldValue.getValue().getTwinClassField() + " will be filled from context");
                factoryItem.getOutputTwin().addField(fieldValue.getValue());
            }
        }
        if (logMsgs.size() > 0)
            log.info(String.join(System.lineSeparator(), logMsgs));
    }
}
