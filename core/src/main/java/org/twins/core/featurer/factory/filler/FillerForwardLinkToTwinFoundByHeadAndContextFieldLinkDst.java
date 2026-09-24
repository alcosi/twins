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
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.LookupResult;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(
        id = FeaturerTwins.ID_2360,
        name = "Forward link to twin found by head and context field link dst",
        description = "Finds twin by head and link dst resolved from context field; creates forward link from output twin."
)
@Slf4j
public class FillerForwardLinkToTwinFoundByHeadAndContextFieldLinkDst extends FillerForwardLinkToTwinFoundByHeadAndLinkDstBase {

    @FeaturerParam(name = "Dst twin class field id", description = "Field to read link dst twin id from context (link field or transition field)", order = 3)
    public static final FeaturerParamUUID dstTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dstTwinClassFieldId");

    @Lazy
    @Autowired
    private TwinClassFieldService twinClassFieldService;

    /**
     * Direct batch override (not the default per-item loop of {@code FillerLinks}): one lookuper
     * batch call per step (bulk preloads + entity resolution once), then the per-item distribution —
     * the lookup failure is re-thrown at the exact point where the per-item body resolved the dst
     * twin — see featurer_design_pattern.md.
     */
    @Override
    public void fill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin, boolean optionalStep) throws ServiceException {
        UUID dstFieldId = dstTwinClassFieldId.extract(properties);
        LookupResult dstFieldValue = fieldLookupers.getFromContextFieldsAndContextTwinDbFields().lookupFieldValue(batch, dstFieldId);
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                fillWith(properties, factoryItem, templateTwin, dstFieldValue);
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
    }

    @Override
    protected UUID getLinkId(Properties properties) throws ServiceException {
        UUID dstFieldId = dstTwinClassFieldId.extract(properties);
        return twinClassFieldService.getConfiguredLinkSafe(dstFieldId);
    }

    @Override
    protected TwinEntity resolveDstTwin(Properties properties, FactoryItem factoryItem, TwinEntity contextTwin) throws ServiceException {
        UUID dstFieldId = dstTwinClassFieldId.extract(properties);
        FieldValue dstFieldValue = fieldLookupers.getFromContextFieldsAndContextTwinDbFields()
                .lookupFieldValue(factoryItem, dstFieldId);
        return FieldValueLink.getSingleLinkedTwinSafe(dstFieldValue);
    }
}

