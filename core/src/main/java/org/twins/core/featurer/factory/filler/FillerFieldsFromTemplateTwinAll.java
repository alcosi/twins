package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapperV2;
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

    @Lazy
    @Autowired
    TwinFieldRestDTOMapperV2 twinFieldRestDTOMapperV2;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutputTwin().getTwinEntity();
        if (!twinClassService.isInstanceOf(outputTwinEntity.getTwinClass(), templateTwin.getTwinClassId()))
            throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "factoryItem output twinClass[" + outputTwinEntity.getTwinClassId() +"] is not instance of template twin class[" + templateTwin.getTwinClassId() + "]");
        List<TwinFieldEntity> twinFieldEntityList = twinService.findTwinFields(templateTwin.getId());
        List<FieldValueText> fields;
        List<String> logMsgs = new ArrayList<>();
        try {
            fields = twinFieldRestDTOMapperV2.convertList(twinFieldEntityList);
            for (FieldValueText fieldValue : fields) {
                logMsgs.add(outputTwinEntity + "" + fieldValue.getTwinClassField() + " will be filled from template");
            }
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_VALUR_INCORRECT);
        }
        if (logMsgs.size() > 0)
            log.info(String.join(System.lineSeparator(), logMsgs));
        factoryItem.getOutputTwin().addFields((List)fields);
//        TwinService.CloneFieldsResult cloneFieldsResult = twinService.cloneTwinFieldList(templateTwin, outputTwinEntity);
//        if (CollectionUtils.isNotEmpty(cloneFieldsResult.getFieldEntityList()))
//            factoryItem.getOutputTwin().addFields(cloneFieldsResult.getFieldEntityList());
//        if (CollectionUtils.isNotEmpty(cloneFieldsResult.getFieldDataListEntityList()))
//            factoryItem.getOutputTwin().addFieldsDataList(cloneFieldsResult.getFieldDataListEntityList());
    }
}
