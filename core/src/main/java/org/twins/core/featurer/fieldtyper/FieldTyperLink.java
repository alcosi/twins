package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinLinkNoRelationsProjection;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.link.TwinLinkService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Lazy
@Component
@Featurer(id = 1310,
        name = "FieldTyperLink",
        description = "")
public class FieldTyperLink extends FieldTyper<FieldDescriptorLink, FieldValueLink> {
    @Autowired
    EntitySmartService entitySmartService;
    @Lazy
    @Autowired
    LinkService linkService;
    @Lazy
    @Autowired
    TwinLinkService twinLinkService;
    @Autowired
    TwinLinkRepository twinLinkRepository;

    @FeaturerParam(name = "linkUUID", description = "")
    public static final FeaturerParamUUID linkUUID = new FeaturerParamUUID("linkUUID");

    @FeaturerParam(name = "longListThreshold", description = "If options count is bigger then given threshold longList type will be used")
    public static final FeaturerParamInt longListThreshold = new FeaturerParamInt("longListThreshold");

    @Override
    protected FieldDescriptorLink getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        LinkEntity linkEntity = linkService.findEntitySafe(linkUUID.extract(properties));
        long listSize = twinLinkService.countValidDstTwins(linkEntity, twinClassFieldEntity.getTwinClass());
        FieldDescriptorLink fieldDescriptorLink = new FieldDescriptorLink()
                .multiple(allowMultiply(linkEntity, twinClassFieldEntity));
        if (listSize > longListThreshold.extract(properties))
            fieldDescriptorLink.linkId(linkEntity.getId());
        else {
            fieldDescriptorLink.dstTwins(twinLinkService.findValidDstTwins(linkEntity, twinClassFieldEntity.getTwinClass()));
        }
        return fieldDescriptorLink;
    }

    protected boolean allowMultiply(LinkEntity linkEntity, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        return linkEntity.getType().isMany() && linkService.isBackwardLink(linkEntity, twinClassFieldEntity.getTwinClass());
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueLink value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        LinkEntity linkEntity = linkService.findEntitySafe(linkUUID.extract(properties));
        List<TwinLinkEntity> newTwinLinks = value.getTwinLinks() != null ? value.getTwinLinks() : new ArrayList<>();
        for (TwinLinkEntity newTwinLinkEntity : newTwinLinks) //we have to set link, because it can be empty
            newTwinLinkEntity
                    .setLinkId(linkEntity.getId())
                    .setLink(linkEntity);
        if (twinFieldEntity.getTwinClassField().isRequired() && CollectionUtils.isEmpty(newTwinLinks))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        if (newTwinLinks.size() > 1 && !allowMultiply(linkEntity, twinFieldEntity.getTwinClassField()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " multiply links are not allowed");
        twinLinkService.prepareTwinLinks(twinFieldEntity.getTwin(), newTwinLinks);
        if (twinFieldEntity.getId() == null) //not new field
            twinFieldEntity.setId(UUID.randomUUID()); // we have to generate id here, because TwinFieldDataListEntity is linked to TwinFieldEntity by FK
        List<TwinLinkNoRelationsProjection> storedLinksList = twinLinkRepository.findBySrcTwinIdAndLinkId(twinFieldEntity.getTwinId(), linkEntity.getId(), TwinLinkNoRelationsProjection.class);
        Map<UUID, TwinLinkNoRelationsProjection> storedLinksMap = null; // key is links dstTwinId
        if (CollectionUtils.isNotEmpty(storedLinksList))
            storedLinksMap = storedLinksList.stream().collect(Collectors.toMap(TwinLinkNoRelationsProjection::dstTwinId, Function.identity()));
        for (TwinLinkEntity twinLinkEntity : newTwinLinks) {
            if (storedLinksMap == null) {  // no links were saved before //after twinLinkService.prepareTwinLinks all existed twinLinks will be filled with id from db
                twinChangesCollector.add(twinLinkEntity);
            } else if (storedLinksMap.containsKey(twinLinkEntity.getDstTwinId())) { // link is already saved
                storedLinksMap.remove(twinLinkEntity.getDstTwinId()); // we remove is from list, because all remained list elements will be deleted from database (pretty logic inversion)
            } else if (twinLinkEntity.getLink().getType().isUniqForSrcTwin()) {
                if (storedLinksMap.size() != 1)
                    throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT, "Multiple links not valid for type[" + twinLinkEntity.getLink().getType().name() + "]");
                TwinLinkNoRelationsProjection dbTwinLink = storedLinksList.get(0);
                log.warn(twinLinkEntity.getLink().logShort() + " is already exists for " + twinLinkEntity.getSrcTwin().logShort() + ". " + dbTwinLink.easyLog(EasyLoggable.Level.NORMAL) + " will be updated");
                twinLinkEntity.setId(dbTwinLink.id());
                twinChangesCollector.add(twinLinkEntity);
                storedLinksMap.clear(); // we remove is from list, because all remained list elements will be deleted from database (pretty logic inversion)
            } else {
                twinChangesCollector.add(twinLinkEntity);
            }
        }
        if (storedLinksMap != null && CollectionUtils.isNotEmpty(storedLinksMap.entrySet())) // old values must be deleted
            twinChangesCollector.deleteAll(TwinLinkEntity.class, storedLinksMap.values().stream().map(TwinLinkNoRelationsProjection::id).toList());
    }

    @Override
    protected FieldValueLink deserializeValue(Properties properties, TwinFieldEntity twinFieldEntity) throws ServiceException {
        FieldValueLink ret = new FieldValueLink();
        LinkEntity linkEntity = linkService.findEntitySafe(linkUUID.extract(properties));
        LinkService.LinkDirection linkDirection = linkService.detectLinkDirection(linkEntity, twinFieldEntity.getTwin().getTwinClass());
        List<TwinLinkEntity> twinLinkEntityList = twinLinkService.findTwinLinks(linkEntity, twinFieldEntity.getTwin(), linkDirection);
        if (twinLinkEntityList != null)
            ret.setTwinLinks(twinLinkEntityList);
        return ret
                .setForwardLink(linkDirection == LinkService.LinkDirection.forward);
    }
}
