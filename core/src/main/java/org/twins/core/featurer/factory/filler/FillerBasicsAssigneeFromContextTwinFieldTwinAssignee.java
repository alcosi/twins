package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2328,
        name = "FillerBasicsAssigneeFromContextTwinFieldTwinAssignee",
        description = "If value of context twin field is an id of other twin (link) we will get assignee from that twin")
@Slf4j
public class FillerBasicsAssigneeFromContextTwinFieldTwinAssignee extends FillerBasicsAssigneeFromContextFieldTwinAssignee {
    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        UUID assigneeFieldId = linkField.extract(properties);
        FieldValue assigneeField = fieldLookupers.fromContextTwinDbFields.lookupFieldValue(factoryItem, assigneeFieldId);
        fill(properties, factoryItem, templateTwin, assigneeField, assigneeFieldId);
    }
}
