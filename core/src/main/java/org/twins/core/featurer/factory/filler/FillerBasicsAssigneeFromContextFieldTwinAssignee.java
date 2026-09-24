package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2322,
        name = "Basics assignee from context field twin assignee",
        description = "If value of context field is an id of other twin (link) we will get assignee from that twin")
@Slf4j
public class FillerBasicsAssigneeFromContextFieldTwinAssignee extends Filler {
    @FeaturerParam(name = "Link field", description = "", order = 1)
    public static final FeaturerParamUUID linkField = new FeaturerParamUUIDTwinsTwinClassFieldId("linkField");

    @Lazy
    @Autowired
    TwinService twinService;

    /**
     * Direct batch override (not a {@code FillerAtomic} subclass): two-phase — first an isolated
     * per-item loop collects the linked twins discovered from the field values (no db access), then
     * ONE bulk {@code loadUser} covers the whole batch, then the in-memory distribution runs; the
     * lookuper-based subclass pre-resolves its field in its own batch override and reuses
     * {@link #assignFromLinkedTwins} — see featurer_design_pattern.md.
     */
    @Override
    public void fill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin, boolean optionalStep) throws ServiceException {
        if (batch == null || batch.isEmpty())
            return;
        UUID assigneeFieldId = linkField.extract(properties);
        var linkedTwins = new HashMap<TwinEntity, TwinEntity>();
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                FieldValue assigneeField = factoryItem.getFactoryContext().getFields().get(assigneeFieldId);
                TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
                TwinEntity linkedTwin = FieldValueLink.getSingleLinkedTwinSafe(assigneeField);
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

    /** Bulk phase shared with the lookuper-based subclass: one loadUser for the whole batch, then the in-memory distribution. */
    protected void assignFromLinkedTwins(Map<TwinEntity, TwinEntity> linkedTwins) throws ServiceException {
        twinService.loadUser(linkedTwins.values());
        for (var entry : linkedTwins.entrySet()) {
            TwinEntity outputTwinEntity = entry.getKey();
            TwinEntity linkedTwin = entry.getValue();
            log.info("{} [assignee] will be filled from twin {}", outputTwinEntity.logShort(), linkedTwin);
            UserEntity assignee = linkedTwin.getAssignerUser();
            if (assignee == null)
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No assignee for twin[" + linkedTwin.getId() + "]");
            outputTwinEntity
                    .setAssignerUser(assignee)
                    .setAssignerUserId(assignee.getId());
        }
    }
}
