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
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;
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

    @Lazy
    @Autowired
    TwinService twinService;

    @Override
    public void fill(Properties properties, Collection<FactoryItem> factoryItems, TwinEntity templateTwin, boolean optional) throws ServiceException {
        // the link to create is step-constant (same newLinksId for every item) -> resolve it once per batch
        LinkEntity link = linkService.findEntitySafe(newLinksId.extract(properties));
        for (FactoryItem factoryItem : factoryItems) {
            try {
                fillItem(properties, factoryItem, link);
            } catch (Exception ex) {
                if (optional && canBeOptional()) {
                    log.warn("Optional filler step failed for {}, skipping: {}", factoryItem.logShort(), (ex instanceof ServiceException serviceException ? serviceException.getErrorLocation() : ex.getMessage()));
                } else {
                    throw ex;
                }
            }
        }
    }

    private void fillItem(Properties properties, FactoryItem factoryItem, LinkEntity link) throws ServiceException {
        UUID extractedSrcTwinClassFieldId = srcTwinClassFieldId.extract(properties);
        FieldValue srcFieldValue = fieldLookupers.getFromContextFields().lookupFieldValue(factoryItem, extractedSrcTwinClassFieldId);
        if (!(srcFieldValue instanceof FieldValueLink fieldValueLink)) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "srcTwinClassField[" + extractedSrcTwinClassFieldId + "] is not instance of link field");
        }
        if (fieldValueLink.isEmpty()) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "srcTwinClassField[" + extractedSrcTwinClassFieldId + "] is not filled");
        }
        TwinEntity dstTwin = resolveDstTwin(fieldValueLink.getItems().getFirst());
        TwinEntity linkDstTwin;
        if (useDstTwinHead.extract(properties)) {
            linkDstTwin = twinService.loadHead(dstTwin);
            if (linkDstTwin == null) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "No head twin detected for twin: " + dstTwin.logDetailed());
            }
        } else {
            linkDstTwin = dstTwin;
        }
        TwinEntity outputTwin = factoryItem.getTwin();
        addLink(factoryItem.getOutput(), TwinLinkEntity.of(link, outputTwin, linkDstTwin));
    }

    private TwinEntity resolveDstTwin(TwinLinkEntity matchedLink) throws ServiceException {
        twinLinkService.loadDstTwin(matchedLink);
        return matchedLink.getDstTwin();
    }
}
