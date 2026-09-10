package org.twins.core.featurer.fieldtyper;

import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorLink;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twinlink.TwinLinkService;

import java.util.*;

/**
 * Shared logic of the link-typed field typers ({@link FieldTyperForwardLink}, {@link FieldTyperBackwardLink}):
 * the field value is a DESIRED set of far twins, written state-based through the standard link pipeline
 * (relation twin lifecycle, history, MANDATORY guard — see TwinLinkService.reconcileLinks) and read from the
 * twin's stored links on the typer's side. The subclass contract: {@link #linkDirection()} (the typer's
 * EXPLICIT intent — never class-detected, a link between the same twin class on both ends is ambiguous) and
 * {@link #isSupportedLinkType(LinkEntity)} (the link types whose side of the twin stays bounded).
 */
public abstract class FieldTyperLinkBase extends FieldTyper<FieldDescriptorLink, FieldValueLink, TwinFieldStorageLink, TwinFieldSearchNotImplemented> {

    @Lazy
    @Autowired
    LinkService linkService;
    @Lazy
    @Autowired
    TwinLinkService twinLinkService;

    @FeaturerParam(name = "Link", description = "", order = 1)
    public static final FeaturerParamUUIDTwinsLinkId linkUUID = new FeaturerParamUUIDTwinsLinkId("linkUUID");

    @FeaturerParam(name = "Long list threshold", description = "If options count is bigger then given threshold longList type will be used", order = 2)
    public static final FeaturerParamInt longListThreshold = new FeaturerParamInt("longListThreshold");

    /**
     * The direction this typer operates in — set EXPLICITLY, never class-detected: a link may be configured
     * between the same twin class on both ends, where both class checks match and only the typer choice
     * (forward vs backward) disambiguates the intent.
     */
    protected abstract LinkService.LinkDirection linkDirection();

    /**
     * The link types this typer may operate on. Forward links are the twin's OWN outgoing links — bounded by
     * the twin's own writes, any type is safe. The backward typer allows OneToOne only: uniqForDstTwin
     * bounds a twin's backward links to at most one, while many-typed backward links are unbounded.
     */
    protected boolean isSupportedLinkType(LinkEntity linkEntity) {
        return true;
    }

    @Override
    protected FieldDescriptorLink getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        LinkEntity linkEntity = linkService.findEntitySafe(linkUUID.extract(properties));
        FieldDescriptorLink fieldDescriptorLink = new FieldDescriptorLink()
                .multiple(allowMultiply(linkEntity, twinClassFieldEntity))
                .linkId(linkEntity.getId());
// todo now only long list supported, because of pagination problems

//        long listSize = twinLinkService.countValidDstTwins(linkEntity, twinClassFieldEntity.getTwinClass());
//        if (listSize > longListThreshold.extract(properties))
//            fieldDescriptorLink.linkId(linkEntity.getId());
//        else {
//            fieldDescriptorLink.dstTwins(twinLinkService.findValidDstTwins(linkEntity, twinClassFieldEntity.getTwinClass()));
//            if(listSize != fieldDescriptorLink.dstTwins().size())
//                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_HIERARCHY_ERROR, twinClassFieldEntity.getTwinClass().getId() + " / " + listSize + " / " + fieldDescriptorLink.dstTwins().size());
//        }
        return fieldDescriptorLink;
    }

    protected boolean allowMultiply(LinkEntity linkEntity, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        return linkEntity.getType().isMany() && linkService.isBackwardLink(linkEntity, twinClassFieldEntity.getTwinClass());
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueLink value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        LinkEntity linkEntity = linkService.findEntitySafe(linkUUID.extract(properties));
        if (!isSupportedLinkType(linkEntity))
            throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT,
                    linkEntity.logShort() + " of type[" + linkEntity.getType() + "] is not supported by " + this.getClass().getSimpleName());
        List<TwinEntity> toTwins = value.getItems() != null ? value.getItems() : new ArrayList<>();
        if (toTwins.size() > 1 && !allowMultiply(linkEntity, value.getTwinClassField()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED, value.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " multiply links are not allowed");
        // state-based write through the standard link pipeline: relation twin lifecycle (empty AUTO twin on
        // create, reuse on relink), link history and the MANDATORY delete guard behave exactly as via the
        // links[] API — no field-specific twin_link logic here. The direction is this typer's explicit intent.
        twinLinkService.reconcileLinks(twin, linkEntity, linkDirection(), toTwins, twinChangesCollector);
    }

    @Override
    protected FieldValueLink deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twinEntity = twinField.getTwin();
        LinkEntity linkEntity = linkService.findEntitySafe(linkUUID.extract(properties));
        if (!isSupportedLinkType(linkEntity)) { // legacy misconfigured field — render as undefined, do not pull links
            FieldValueLink undefined = new FieldValueLink(twinField.getTwinClassField());
            undefined.undefine();
            undefined.setForwardLink(linkDirection() == LinkService.LinkDirection.forward);
            return undefined;
        }
        List<TwinLinkEntity> twinLinkEntityList = storedLinks(twinEntity, linkEntity);
        FieldValueLink ret = new FieldValueLink(twinField.getTwinClassField());
        if (CollectionUtils.isNotEmpty(twinLinkEntityList)) {
            // the far twins ARE the field value — the twin_link to each is rebuilt on serialize
            if (linkDirection() == LinkService.LinkDirection.forward)
                twinLinkService.loadDstTwin(twinLinkEntityList);
            else
                twinLinkService.loadSrcTwin(twinLinkEntityList);
            List<TwinEntity> farTwins = new ArrayList<>(twinLinkEntityList.size());
            for (TwinLinkEntity twinLinkEntity : twinLinkEntityList)
                farTwins.add(linkDirection() == LinkService.LinkDirection.forward ? twinLinkEntity.getDstTwin() : twinLinkEntity.getSrcTwin());
            ret.setItems(farTwins);
        } else {
            ret.undefine();
        }
        ret.setForwardLink(linkDirection() == LinkService.LinkDirection.forward);
        return ret;
    }

    /**
     * The stored twin_links of (twin, link) on this typer's side: forward — the twin's own outgoing links
     * (bounded by the twin's own writes); backward — the incoming links, queried dst-side only because the
     * backward typer is OneToOne-restricted (at most one).
     */
    protected List<TwinLinkEntity> storedLinks(TwinEntity twinEntity, LinkEntity linkEntity) throws ServiceException {
        if (linkDirection() == LinkService.LinkDirection.backward)
            return new ArrayList<>(twinLinkService.findTwinLinks(linkEntity, twinEntity, LinkService.LinkDirection.backward));
        twinLinkService.loadTwinLinks(twinEntity);
        return twinEntity.getTwinLinks().getForwardLinks().getGrouped(linkEntity.getId());
    }

    public UUID getLinkId(HashMap<String, String> fieldTyperParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldTyperParams);
        return getLinkId(properties);
    }

    public UUID getLinkId(Properties properties) throws ServiceException {
        return linkUUID.extract(properties);
    }
}
