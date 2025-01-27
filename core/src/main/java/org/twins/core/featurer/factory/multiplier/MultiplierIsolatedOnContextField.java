package org.twins.core.featurer.factory.multiplier;

import org.apache.commons.collections4.MapUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2203,
        name = "IsolatedOnContextField",
        description = "New output twin for each input. Output class is selected by checking if given twinClassField is present in context")
public class MultiplierIsolatedOnContextField extends Multiplier {
    @FeaturerParam(name = "Output twin class id from context field", description = "", order = 1)
    public static final FeaturerParamUUID outputTwinClassIdFromContextField = new FeaturerParamUUIDTwinsTwinClassFieldId("outputTwinClassIdFromContextField");
    @FeaturerParam(name = "Else output twin class id", description = "", order = 2)
    public static final FeaturerParamUUID elseOutputTwinClassId = new FeaturerParamUUIDTwinsTwinClassId("elseOutputTwinClassId");

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        FieldValue fieldValue = MapUtils.getObject(factoryContext.getFields(), outputTwinClassIdFromContextField.extract(properties));
        UUID outputTwinClassId;
        if (fieldValue != null)
            outputTwinClassId = fieldValue.getTwinClassField().getTwinClassId();
        else
            outputTwinClassId = elseOutputTwinClassId.extract(properties);
        TwinClassEntity outputTwinClassEntity = twinClassService.findEntitySafe(outputTwinClassId);
        ApiUser apiUser = authService.getApiUser();
        List<FactoryItem> ret = new ArrayList<>();
        for (FactoryItem inputItem : inputFactoryItemList) {
            TwinEntity newTwin = new TwinEntity()
                    .setName("")
                    .setTwinClass(outputTwinClassEntity)
                    .setTwinClassId(outputTwinClassEntity.getId())
                    .setCreatedAt(Timestamp.from(Instant.now()))
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
