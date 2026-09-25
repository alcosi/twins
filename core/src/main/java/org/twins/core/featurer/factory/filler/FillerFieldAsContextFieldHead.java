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
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.LookupResult;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;


@Component
@Featurer(id = FeaturerTwins.ID_2334,
        name = "Field as context field head",
        description = "Get head for twin from src field(link). Set this head to dst field(link)")
@Slf4j
public class FillerFieldAsContextFieldHead extends Filler {

    @FeaturerParam(name = "Src twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID srcTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("srcTwinClassFieldId");

    @FeaturerParam(name = "Dst twin class field id", description = "", order = 2)
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    @Lazy
    @Autowired
    TwinService twinService;

    /**
     * Direct batch override, two-phase (not a {@code FillerFieldLookup} subclass — the linked twins
     * are discovered per item, so the head load can only be bulk after collecting them): one
     * lookuper batch call, then an isolated per-item collection loop (no db access), then ONE bulk
     * {@code loadHead}, then the in-memory distribution — see featurer_design_pattern.md.
     */
    @Override
    public void fill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin, boolean optionalStep) throws ServiceException {
        if (batch == null || batch.isEmpty())
            return;
        LookupResult result = fieldLookupers.getFromContextFields().lookupFieldValue(batch, srcTwinClassFieldId.extract(properties));
        var linkedTwins = new HashMap<FactoryItem, TwinEntity>();
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                result.rethrowFailureIfPresent(factoryItem); // original error, original per-item isolation
                FieldValue srcFieldValue = result.value(factoryItem);
                srcFieldValue.assertIsDefined(srcFieldValue.getTwinClassField().logNormal() + " is not present in context fields");
                linkedTwins.put(factoryItem, FieldValueLink.getSingleLinkedTwinSafe(srcFieldValue));
            } catch (Exception ex) {
                handleItemError(factoryItem, optionalStep, ex);
            }
        }
        twinService.loadHead(linkedTwins.values()); // one query for the whole batch
        UUID dstFieldId = dstTwinClassFieldId.extract(properties);
        for (var entry : linkedTwins.entrySet()) {
            FactoryItem factoryItem = entry.getKey();
            TwinEntity linkedTwin = entry.getValue();
            var detectedHeadTwin = linkedTwin.getHeadTwin();
            if (detectedHeadTwin == null)
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No head twin detected for " + linkedTwin.logDetailed());
            factoryItem.getOutput().addField(twinService.createFieldValue(dstFieldId, detectedHeadTwin));
        }
    }
}
