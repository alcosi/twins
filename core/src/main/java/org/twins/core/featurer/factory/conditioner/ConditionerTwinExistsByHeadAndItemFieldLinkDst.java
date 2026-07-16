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
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2452,
        name = "Twin exists by head and item field link dst",
        description = "True if twin exists with same head twin and link dst twin resolved from item field.")
@Slf4j
public class ConditionerTwinExistsByHeadAndItemFieldLinkDst extends ConditionerTwinExistsByHeadAndLinkDstBase {

    @FeaturerParam(name = "Dst twin class field id", description = "Field to read link dst twin id from context (link field or transition field)", order = 3)
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    @Override
    protected UUID resolveHeadTwinId(TwinEntity contextTwin) {
        return contextTwin.getHeadTwinId() != null ? contextTwin.getHeadTwinId() : contextTwin.getId();
    }

    @Override
    protected UUID resolveDstTwinId(Properties properties, FactoryItem factoryItem, TwinEntity contextTwin) throws ServiceException {
        UUID dstFieldId = dstTwinClassFieldId.extract(properties);
        FieldValue dstFieldValue = fieldLookupers.getFromItemOutputFields()
                .lookupFieldValue(factoryItem, dstFieldId);
        TwinEntity dstTwin = extractTwinFromFieldValue(dstFieldValue);
        if (dstTwin == null) {
            log.debug("Link dst twin id is not resolved from context field [{}]", dstFieldId);
            return null;
        }

        return dstTwin.getId();
    }
}
