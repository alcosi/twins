package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Objects;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2430,
        name = "Context item twin assignee equals context twin field link assignee",
        description = "")
@Slf4j
public class ConditionerContextItemTwinAssigneeEqualsContextTwinFieldLinkAssignee extends Conditioner {
    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        var fieldValue = fieldLookupers.getFromContextFields().lookupFieldValue(factoryItem, twinClassFieldId.extract(properties));
        TwinEntity linkedTwin = FieldValueLink.getSingleLinkedTwinSafe(fieldValue);
        return Objects.equals(linkedTwin.getAssignerUserId(), factoryItem.checkSingleContextItem().getTwin().getAssignerUserId());
    }
}
