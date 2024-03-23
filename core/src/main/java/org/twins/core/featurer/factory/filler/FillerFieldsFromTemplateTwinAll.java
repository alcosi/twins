package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
@Featurer(id = 2309,
        name = "FillerFieldsFromTemplateTwinAll",
        description = "")
@Slf4j
public class FillerFieldsFromTemplateTwinAll extends Filler {
    @Lazy
    @Autowired
    TwinClassService twinClassService;

    @Lazy
    @Autowired
    TwinService twinService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        if (!twinClassService.isInstanceOf(outputTwinEntity.getTwinClass(), templateTwin.getTwinClassId()))
            throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "factoryItem output twinClass[" + outputTwinEntity.getTwinClassId() +"] is not instance of template twin class[" + templateTwin.getTwinClassId() + "]");
        twinService.loadTwinFields(templateTwin);
        if (templateTwin.getTwinFieldKit().isEmpty() && templateTwin.getTwinFieldUserKit().isEmpty() && templateTwin.getTwinFieldDatalistKit().isEmpty())
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No template twin fields present. Please check fields for " + templateTwin.logShort());
        List<TwinField> twinFieldList = new ArrayList<>();
        if (templateTwin.getTwinFieldKit().isNotEmpty())
            twinFieldList.a
        List<FieldValue> cloneFieldList = new ArrayList<>();
        List<String> logMsgs = new ArrayList<>();
        for (TwinField srcField : twinFieldList) {
            FieldValue fieldValue = twinService.getTwinFieldValue(srcField);
            FieldValue clone = fieldValue.clone();
            cloneFieldList.add(clone);
            logMsgs.add(outputTwinEntity.logShort() + " " + fieldValue.getTwinClassField().logNormal() + " will be filled from template");
        }
        if (logMsgs.size() > 0)
            log.info(String.join(System.lineSeparator(), logMsgs));
        factoryItem.getOutput().addFields(cloneFieldList);
    }
}
