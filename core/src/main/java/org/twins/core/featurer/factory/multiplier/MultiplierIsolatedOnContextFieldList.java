package org.twins.core.featurer.factory.multiplier;

import org.apache.commons.collections4.MapUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2206,
        name = "IsolatedOnContextFieldList",
        description = "New output twin for each input. Output class is selected by checking if fields in context (in loop). Order is important." +
                "If field is present then output twin class will be selected from this field class, otherwise loop will continue")
public class MultiplierIsolatedOnContextFieldList extends Multiplier {
    @FeaturerParam(name = "Context field list", description = "", order = 1)
    public static final FeaturerParamUUIDSet contextFieldIdList = new FeaturerParamUUIDSetTwinsTwinClassFieldId("contextFieldIdList");

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        UUID outputTwinClassId = null;
        //detecting twinClass in loop. order is important
        for (UUID fieldId : contextFieldIdList.extract(properties)) {
            FieldValue fieldValue = MapUtils.getObject(factoryContext.getFields(), fieldId);
            if (fieldValue != null)
                outputTwinClassId = fieldValue.getTwinClassField().getTwinClassId();

        }
        if (outputTwinClassId == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_MULTIPLIER_ERROR, "can not detect twin class. No one of expected fields[" + properties.get(contextFieldIdList.getKey()) + "] presents in context");
        TwinClassEntity outputTwinClassEntity = twinClassService.findEntitySafe(outputTwinClassId);
        ApiUser apiUser = authService.getApiUser();
        List<FactoryItem> ret = new ArrayList<>();
        for (FactoryItem inputItem : inputFactoryItemList) {
            TwinEntity newTwin = new TwinEntity()
                    .setName("")
                    .setTwinClass(outputTwinClassEntity)
                    .setTwinClassId(outputTwinClassEntity.getId())
                    .setCreatedByUserId(apiUser.getUser().getId())
                    .setCreatedByUser(apiUser.getUser());
            TwinCreate twinCreate = new TwinCreate();
            twinCreate.setTwinEntity(newTwin);
            ret.add(new FactoryItem()
                    .setOutput(twinCreate)
                    .setContextFactoryItemList(List.of(inputItem)));
        }
        return ret;
    }
}
