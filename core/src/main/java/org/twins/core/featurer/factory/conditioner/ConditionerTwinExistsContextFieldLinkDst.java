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
import org.twins.core.service.twin.TwinSearchServiceV2;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2449,
        name = "Twin exists by head of head and context field link dst",
        description = "True if twin exists with same head twin and link dst twin resolved from context field.")
@Slf4j
public class ConditionerTwinExistsContextFieldLinkDst extends ConditionerTwinExistsByHeadAndLinkDstBase {

    @FeaturerParam(name = "Dst twin class field id", description = "Field to read link dst twin id from context (link field or transition field)", order = 3)
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    public ConditionerTwinExistsContextFieldLinkDst(TwinSearchServiceV2 twinSearchService, TwinService twinService) {
        super(twinSearchService, twinService);
    }

    @Override
    protected UUID resolveHeadTwinId(TwinEntity contextTwin) {
        if (contextTwin.getHeadTwinId() != null) {
            TwinEntity headTwin = contextTwin.getHeadTwin();
            if (headTwin == null) {
                headTwin = twinService.findHeadTwin(contextTwin.getHeadTwinId());
            }
            if (headTwin != null && headTwin.getHeadTwinId() != null) {
                return headTwin.getHeadTwinId();
            }
            return null;
        }
        return null;
    }

    @Override
    protected UUID resolveDstTwinId(Properties properties, FactoryItem factoryItem, TwinEntity contextTwin) throws ServiceException {
        UUID dstFieldId = dstTwinClassFieldId.extract(properties);
        FieldValue dstFieldValue = fieldLookupers.getFromContextFieldsAndContextTwinDbFields()
                .lookupFieldValue(factoryItem, dstFieldId);
        TwinEntity childTwin = extractTwinFromFieldValue(dstFieldValue);

        if (childTwin == null) {
            log.debug("Link dst twin id is not resolved from context field [{}]", dstFieldId);
            return null;
        }
        UUID headTwinId = childTwin.getHeadTwinId();
        if (headTwinId == null) {
            log.debug("Head twin id is not resolved for twin [{}] from context field [{}]", childTwin.getId(), dstFieldId);
        }

        return headTwinId;
    }
}
