package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.domain.twinlink.TwinLinkCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorLink;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twinlink.TwinLinkService;

import java.util.*;



@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_1310,
        name = "Linked twin",
        description = "")
public class FieldTyperLink extends FieldTyper<FieldDescriptorLink, FieldValueLink, TwinFieldStorageLink, TwinFieldSearchNotImplemented> {
    public static final Integer ID = 1310;

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

    //todo check if this method works correctly for fields that display backward links
    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueLink value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        LinkEntity linkEntity = linkService.findEntitySafe(linkUUID.extract(properties));
        List<TwinLinkEntity> newTwinLinks = value.getItems() != null ? value.getItems() : new ArrayList<>();
        for (TwinLinkEntity newTwinLinkEntity : newTwinLinks) //we have to set link, because it can be empty
            newTwinLinkEntity
                    .setLinkId(linkEntity.getId())
                    .setLink(linkEntity);
        if (newTwinLinks.size() > 1 && !allowMultiply(linkEntity, value.getTwinClassField()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED, value.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " multiply links are not allowed");
        // state-based write through the standard link pipeline: relation twin lifecycle (empty AUTO twin on
        // create, reuse on relink), link history and the MANDATORY delete guard behave exactly as via the
        // links[] API — no field-specific twin_link logic here
        twinLinkService.reconcileLinks(twin, linkEntity, TwinLinkCreate.wrapAll(newTwinLinks), twinChangesCollector);
    }

    @Override
    protected FieldValueLink deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twinEntity = twinField.getTwin();
        LinkEntity linkEntity = linkService.findEntitySafe(linkUUID.extract(properties));
        LinkService.LinkDirection linkDirection = linkService.detectLinkDirection(linkEntity, twinField.getTwin().getTwinClass());
        twinLinkService.loadTwinLinks(twinEntity);
        List<TwinLinkEntity> twinLinkEntityList;
        if (linkDirection == LinkService.LinkDirection.forward)
            twinLinkEntityList = twinEntity.getTwinLinks().getForwardLinks().getGrouped(linkEntity.getId());
        else
            twinLinkEntityList = twinEntity.getTwinLinks().getBackwardLinks().getGrouped(linkEntity.getId());
        FieldValueLink ret = new FieldValueLink(twinField.getTwinClassField());
        if (CollectionUtils.isNotEmpty(twinLinkEntityList)) {
            ret.setItems(twinLinkEntityList);
        } else {
            ret.undefine();
        }
        ret.setForwardLink(linkDirection == LinkService.LinkDirection.forward);
        return ret;
    }

    public UUID getLinkId(HashMap<String, String> fieldTyperParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldTyperParams);
        return getLinkId(properties);
    }

    public UUID getLinkId(Properties properties) throws ServiceException {
        return linkUUID.extract(properties);
    }
}
