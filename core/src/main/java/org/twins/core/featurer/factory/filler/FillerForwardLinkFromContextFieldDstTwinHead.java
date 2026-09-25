package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;
import org.twins.core.featurer.factory.lookuper.LookupResult;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.params.FeaturerParamStringTwinsFactoryFieldLookuper;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;

import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(
        id = FeaturerTwins.ID_2355,
        name = "Forward link from context field dst twin head",
        description = "Reads link field from transition context. " +
                "Resolves dst twin (by entity or id). " +
                "Creates new forward link from output twin pointing to dst twin or its head"
)
@Slf4j
public class FillerForwardLinkFromContextFieldDstTwinHead extends FillerLinks {

    @FeaturerParam(name = "Src twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID srcTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("srcTwinClassFieldId");

    @FeaturerParam(name = "New links id", description = "", order = 2)
    public static final FeaturerParamUUID newLinksId = new FeaturerParamUUIDTwinsLinkId("newLinksId");

    @FeaturerParam(name = "Use dst twin head", description = "If true, link dst is head of resolved twin; if false, resolved twin itself", order = 3, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean useDstTwinHead = new FeaturerParamBoolean("useDstTwinHead");

    @FeaturerParam(name = "Field lookuper", description = "Source of the field value", order = 99, optional = true, defaultValue = "fromContextFields")
    public static final FeaturerParamStringTwinsFactoryFieldLookuper fieldLookuperParam = new FeaturerParamStringTwinsFactoryFieldLookuper("fieldLookuper");

    @Lazy
    @Autowired
    TwinService twinService;

    /**
     * Direct batch override, two-phase (not the default per-item loop of {@link FillerLinks}): one
     * lookuper batch call, the new-link entity resolved once per step (param constant), then an
     * isolated per-item collection of the dst twins (assert + navigation, no db access), then ONE
     * bulk {@code loadHead} when the link points at the dst twin's head, then the in-memory
     * distribution — see featurer_design_pattern.md.
     */
    @Override
    public void fill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin, boolean optionalStep) throws ServiceException {
        if (batch == null || batch.isEmpty())
            return;
        UUID extractedSrcTwinClassFieldId = srcTwinClassFieldId.extract(properties);
        boolean useHead = useDstTwinHead.extract(properties);
        LinkEntity link = linkService.findEntitySafe(newLinksId.extract(properties)); // step constant — one lookup per step
        LookupResult srcResult = ((FieldLookuperNearest) fieldLookupers.getByType(fieldLookuperParam.extract(properties)))
                .lookupFieldValue(batch, extractedSrcTwinClassFieldId);
        var dstTwins = new LinkedHashMap<FactoryItem, TwinEntity>();
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                srcResult.rethrowFailureIfPresent(factoryItem); // original error, original per-item isolation
                FieldValue srcFieldValue = srcResult.value(factoryItem);
                FieldValue.assertIsDefined(srcFieldValue, "Src twin class field[" + extractedSrcTwinClassFieldId + "] is not present in context fields");
                dstTwins.put(factoryItem, FieldValueLink.getSingleLinkedTwinSafe(srcFieldValue));
            } catch (Exception ex) {
                handleItemError(factoryItem, optionalStep, ex);
            }
        }
        if (useHead)
            twinService.loadHead(dstTwins.values()); // one query for the whole batch
        for (var entry : dstTwins.entrySet()) {
            FactoryItem factoryItem = entry.getKey();
            TwinEntity dstTwin = entry.getValue();
            TwinEntity linkDstTwin = dstTwin;
            if (useHead) {
                linkDstTwin = dstTwin.getHeadTwin();
                if (linkDstTwin == null)
                    throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No head twin detected for twin: " + dstTwin.logDetailed());
            }
            TwinEntity outputTwin = factoryItem.getTwin();
            TwinLinkEntity newLink = new TwinLinkEntity()
                    .setLink(link)
                    .setLinkId(link.getId())
                    .setSrcTwinId(outputTwin.getId())
                    .setSrcTwin(outputTwin)
                    .setDstTwin(linkDstTwin)
                    .setDstTwinId(linkDstTwin.getId());
            addLink(factoryItem.getOutput(), newLink);
        }
    }

}
