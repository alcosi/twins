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
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2366,
        name = "Field as item output linked twin head",
        description = "")
@Slf4j
public class FillerFieldAsItemOutputLinkedTwinHead extends Filler {
    @Lazy
    @Autowired
    TwinService twinService;

    @FeaturerParam(name = "linkedTwinByTwinClassFieldId", description = "")
    public static final FeaturerParamUUID linkedTwinByTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("linkedTwinByTwinClassFieldId");

    @FeaturerParam(name = "dstTwinClassFieldId", description = "")
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

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
        LookupResult result = fieldLookupers.getFromItemOutputFields().lookupFieldValue(batch, linkedTwinByTwinClassFieldId.extract(properties));
        var linkedTwins = new HashMap<FactoryItem, TwinEntity>();
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                result.rethrowFailureIfPresent(factoryItem); // original error, original per-item isolation
                linkedTwins.put(factoryItem, FieldValueLink.getSingleLinkedTwinSafe(result.value(factoryItem)));
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
        twinService.loadHead(linkedTwins.values()); // one query for the whole batch
        UUID dstFieldId = dstTwinClassFieldId.extract(properties);
        for (var entry : linkedTwins.entrySet()) {
            FactoryItem factoryItem = entry.getKey();
            TwinEntity linkedTwin = entry.getValue();
            var detectedHeadTwin = linkedTwin.getHeadTwin();
            if (detectedHeadTwin == null) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "{} does not contain a head twin", linkedTwin.logShort());
            }
            factoryItem.getOutput().addField(twinService.createFieldValue(dstFieldId, detectedHeadTwin));
        }
    }
}
