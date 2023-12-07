package org.twins.core.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinCreate;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = 2203,
        name = "MultiplierIsolatedOnContextField",
        description = "New output twin for each input. Output class is selected by checking if given twinClassField is present in context")
public class MultiplierIsolatedOnContextField extends Multiplier {
    @FeaturerParam(name = "outputTwinClassIdFromContextField", description = "")
    public static final FeaturerParamUUID outputTwinClassIdFromContextField = new FeaturerParamUUID("outputTwinClassIdFromContextField");
    @FeaturerParam(name = "elseOutputTwinClassId", description = "")
    public static final FeaturerParamUUID elseOutputTwinClassId = new FeaturerParamUUID("elseOutputTwinClassId");
    @Override
    public List<FactoryItem> multiply(Properties properties, List<TwinEntity> inputTwinList, FactoryContext factoryContext) throws ServiceException {
        FieldValue fieldValue = factoryContext.getFields().get(outputTwinClassIdFromContextField.extract(properties));
        UUID outputTwinClassId;
        if (fieldValue != null)
            outputTwinClassId = fieldValue.getTwinClassField().getTwinClassId();
        else
            outputTwinClassId = elseOutputTwinClassId.extract(properties);
        TwinClassEntity outputTwinClassEntity = twinClassService.findEntitySafe(outputTwinClassId);
        ApiUser apiUser = authService.getApiUser();
        List<FactoryItem> ret = new ArrayList<>();
        for (TwinEntity inputTwin : inputTwinList) {
            TwinEntity newTwin = new TwinEntity()
                    .setTwinClass(outputTwinClassEntity)
                    .setTwinClassId(outputTwinClassEntity.getId())
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setCreatedByUserId(apiUser.getUser().getId())
                    .setCreatedByUser(apiUser.getUser());
            TwinCreate twinCreate = new TwinCreate();
            twinCreate.setTwinEntity(newTwin);
            ret.add(new FactoryItem()
                    .setOutputTwin(twinCreate)
                    .setContextTwinList(List.of(inputTwin)));
        }
        return ret;
    }
}
