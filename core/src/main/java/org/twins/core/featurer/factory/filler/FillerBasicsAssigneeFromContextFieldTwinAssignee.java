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
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;
import org.twins.core.featurer.factory.lookuper.LookupResult;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.params.FeaturerParamStringTwinsFactoryFieldLookuper;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2322,
        name = "Basics assignee from linked twin",
        description = "If value of the link field is an id of other twin (link) we will get assignee from that twin. "
                + "The value source is the optional fieldLookuper param")
@Slf4j
public class FillerBasicsAssigneeFromContextFieldTwinAssignee extends Filler {
    @FeaturerParam(name = "Link field", description = "", order = 1)
    public static final FeaturerParamUUID linkField = new FeaturerParamUUIDTwinsTwinClassFieldId("linkField");

    @FeaturerParam(name = "Field lookuper", description = "Source of the link field value", order = 99, optional = true, defaultValue = "fromContextFields")
    public static final FeaturerParamStringTwinsFactoryFieldLookuper fieldLookuperParam = new FeaturerParamStringTwinsFactoryFieldLookuper("fieldLookuper");

    @Lazy
    @Autowired
    TwinService twinService;

    /**
     * Direct batch override: one lookuper batch call (bulk preloads + entity resolution once), then
     * an isolated per-item collection of the linked twins (no db access) — the assignee users are
     * only known per item, so ONE bulk {@code loadUser} covers the whole batch — then the in-memory
     * distribution under the same per-item isolation — see featurer_design_pattern.md.
     */
    @Override
    public void fill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin, boolean optionalStep) throws ServiceException {
        if (batch == null || batch.isEmpty())
            return;
        UUID assigneeFieldId = linkField.extract(properties);
        LookupResult result = ((FieldLookuperNearest) fieldLookupers.getByType(fieldLookuperParam.extract(properties)))
                .lookupFieldValue(batch, assigneeFieldId);
        var linkedTwins = new LinkedHashMap<FactoryItem, TwinEntity>();
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                result.rethrowFailureIfPresent(factoryItem); // original error, original per-item isolation
                FieldValue assigneeField = result.value(factoryItem);
                assigneeField.assertIsDefined(assigneeField.getTwinClassField().logNormal() + " is not found by fieldLookuper");
                linkedTwins.put(factoryItem, FieldValueLink.getSingleLinkedTwinSafe(assigneeField));
            } catch (Exception ex) {
                handleItemError(factoryItem, optionalStep, ex);
            }
        }
        twinService.loadUser(linkedTwins.values()); // one query for the whole batch
        for (Map.Entry<FactoryItem, TwinEntity> entry : linkedTwins.entrySet()) {
            FactoryItem factoryItem = entry.getKey();
            try {
                TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
                TwinEntity linkedTwin = entry.getValue();
                log.info("{} [assignee] will be filled from twin {}", outputTwinEntity.logShort(), linkedTwin);
                UserEntity assignee = linkedTwin.getAssignerUser();
                if (assignee == null)
                    throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No assignee for twin[" + linkedTwin.getId() + "]");
                outputTwinEntity
                        .setAssignerUser(assignee)
                        .setAssignerUserId(assignee.getId());
            } catch (Exception ex) {
                handleItemError(factoryItem, optionalStep, ex);
            }
        }
    }
}
