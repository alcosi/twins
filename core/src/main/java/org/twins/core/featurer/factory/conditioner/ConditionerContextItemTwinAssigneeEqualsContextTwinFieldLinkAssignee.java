package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2430,
        name = "Context item twin assignee equals context twin field link assignee",
        description = "")
@Slf4j
public class ConditionerContextItemTwinAssigneeEqualsContextTwinFieldLinkAssignee extends Conditioner {

    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @Lazy
    @Autowired
    AuthService authService;

    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    TwinLinkService twinLinkService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        FieldValueLink fieldValue = (FieldValueLink) fieldLookupers.getFromContextFields().lookupFieldValue(factoryItem, twinClassFieldId.extract(properties));
        TwinLinkEntity twinLinkEntity = fieldValue.getItems().getFirst();
        TwinEntity dstTwin = twinLinkEntity.getDstTwin();
        if (dstTwin == null) {
            dstTwin = twinService.findEntitySafe(twinLinkEntity.getDstTwinId());
            twinLinkEntity.setDstTwin(dstTwin);
        }
        return dstTwin.getAssignerUserId().equals(factoryItem.checkSingleContextItem().getTwin().getAssignerUserId());
    }
}
