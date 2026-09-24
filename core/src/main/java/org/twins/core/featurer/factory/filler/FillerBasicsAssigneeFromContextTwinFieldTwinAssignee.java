package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.LookupResult;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2328,
        name = "Basics assignee from context twin field twin assignee",
        description = "If value of context twin field is an id of other twin (link) we will get assignee from that twin")
@Slf4j
public class FillerBasicsAssigneeFromContextTwinFieldTwinAssignee extends FillerBasicsAssigneeFromContextFieldTwinAssignee {

    /**
     * Direct batch override, two-phase like the parent: one lookuper batch call (bulk preloads +
     * entity resolution once), then an isolated per-item loop collecting the linked twins from the
     * resolved values (no db access), then the shared bulk {@code loadUser} + distribution — see
     * featurer_design_pattern.md.
     */
    @Override
    public void fill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin, boolean optionalStep) throws ServiceException {
        if (batch == null || batch.isEmpty())
            return;
        UUID assigneeFieldId = linkField.extract(properties);
        LookupResult result = fieldLookupers.getFromContextTwinDbFields().lookupFieldValue(batch, assigneeFieldId);
        var linkedTwins = new HashMap<TwinEntity, TwinEntity>();
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                result.rethrowFailureIfPresent(factoryItem); // original error, original per-item isolation
                TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
                TwinEntity linkedTwin = FieldValueLink.getSingleLinkedTwinSafe(result.value(factoryItem));
                linkedTwins.put(outputTwinEntity, linkedTwin);
            } catch (Exception ex) {
                if (optionalStep) {
                    log.warn("Step is optional and unsuccessful for {}: {}. Pipeline will not be aborted",
                            factoryItem.logShort(),
                            ex instanceof ServiceException serviceException ? serviceException.getErrorLocation() : ex.getMessage());
                } else {
                    throw ex;
                }
            }
        }
        assignFromLinkedTwins(linkedTwins);
    }
}
