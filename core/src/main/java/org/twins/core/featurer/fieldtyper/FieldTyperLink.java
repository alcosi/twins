package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Lazy
@Component
@Featurer(id = 1310,
        name = "FieldTyperLink",
        description = "")
public class FieldTyperLink extends FieldTyper<FieldDescriptorLink, FieldValueLink, TwinLinkEntity> {
    public static final Integer ID = 1310;

    @Autowired
    EntitySmartService entitySmartService;
    @Lazy
    @Autowired
    LinkService linkService;
    @Lazy
    @Autowired
    TwinLinkService twinLinkService;
    @Lazy
    @Autowired
    TwinService twinService;
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

    //todo check if this method works correctly for fields that display backward links
    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueLink value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        LinkEntity linkEntity = linkService.findEntitySafe(linkUUID.extract(properties));
        List<TwinLinkEntity> newTwinLinks = value.getTwinLinks() != null ? value.getTwinLinks() : new ArrayList<>();
        for (TwinLinkEntity newTwinLinkEntity : newTwinLinks) //we have to set link, because it can be empty
            newTwinLinkEntity
                    .setLinkId(linkEntity.getId())
                    .setLink(linkEntity);
        if (value.getTwinClassField().isRequired() && CollectionUtils.isEmpty(newTwinLinks))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, value.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        if (newTwinLinks.size() > 1 && !allowMultiply(linkEntity, value.getTwinClassField()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED, value.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " multiply links are not allowed");
        twinLinkService.prepareTwinLinks(twin, newTwinLinks);
        LinkService.LinkDirection linkDirection = linkService.detectLinkDirection(linkEntity, twin.getTwinClass());
        List<TwinLinkEntity> storedLinksList;
        twinLinkService.loadTwinLinks(twin); //todo optimize loading for backward links
        Map<UUID, TwinLinkEntity> storedLinksMap = null; // key is links dstTwinId
        switch (linkDirection) {
            case forward:
                storedLinksList = twin.getTwinLinks().getForwardLinks().getList();
                if (CollectionUtils.isNotEmpty(storedLinksList))
                    storedLinksMap = storedLinksList.stream().collect(Collectors.toMap(TwinLinkEntity::getDstTwinId, Function.identity()));
                break;
            case backward:
                storedLinksList = twin.getTwinLinks().getBackwardLinks().getList();
                if (CollectionUtils.isNotEmpty(storedLinksList))
                    storedLinksMap = storedLinksList.stream().collect(Collectors.toMap(TwinLinkEntity::getSrcTwinId, Function.identity()));
                break;
            default:
                throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT, linkEntity.logShort() + " can not detect link direction for " + twin.getTwinClass().logShort());
        }
        if (FieldValueChangeHelper.isSingleValueAdd(newTwinLinks, storedLinksMap)) {
            TwinLinkEntity twinLinkEntity = newTwinLinks.get(0);
            twinChangesCollector.add(twinLinkEntity);
            twinChangesCollector.getHistoryCollector().add(historyService.linkCreated(twinLinkEntity));
            return;
        }
        if (FieldValueChangeHelper.isAnyToSingleValueUpdate(newTwinLinks, storedLinksMap)) {
            TwinLinkEntity newLink = newTwinLinks.get(0); //wh have only one element
            TwinLinkEntity storedLink = MapUtils.pullAny(storedLinksMap); // we will update any of existed link it doesn't matter which one. all other will be deleted
            if (!TwinLinkService.equalsInSrcTwinIdAndDstTwinId(newLink, storedLink)) {
                newLink.setId(storedLink.getId());
                if (linkDirection == LinkService.LinkDirection.forward) {
                    log.info(storedLink.easyLog(EasyLoggable.Level.SHORT) + " is already exists and dstTwin will be updated to " + newLink.getDstTwinId());
                    twinChangesCollector.getHistoryCollector().add(historyService.linkUpdated(newLink, storedLink.getDstTwin(), true));
                } else {
                    log.info(storedLink.easyLog(EasyLoggable.Level.SHORT) + " is already exists and srcTwin will be updated to " + newLink.getSrcTwinId());
                    twinChangesCollector.getHistoryCollector().add(historyService.linkUpdated(newLink, storedLink.getSrcTwin(), false));
                }
                twinChangesCollector.add(newLink);
            }
            deleteOutOfDateLinks(twinChangesCollector, storedLinksMap);
            return;
        }
        UUID linkTargetTwinId; // it will be different for link direction
        //removing not changes links
        if (MapUtils.isNotEmpty(storedLinksMap)) {
            Iterator<TwinLinkEntity> iterator = newTwinLinks.listIterator();
            while (iterator.hasNext()) {
                TwinLinkEntity twinLinkEntity = iterator.next();
                if (linkDirection == LinkService.LinkDirection.forward)
                    linkTargetTwinId = twinLinkEntity.getDstTwinId();
                else
                    linkTargetTwinId = twinLinkEntity.getSrcTwinId();
                if (storedLinksMap.containsKey(linkTargetTwinId)) {
                    storedLinksMap.remove(linkTargetTwinId); // if link is already saved we remove is from list, because all remained list elements will be deleted from database (pretty logic inversion)
                    iterator.remove(); // also we need to remove it newLinks list, there is no need to save it
                }
            }
        }
        // here we have storedLinksMap either empty, either with out-of-dated elements
        for (TwinLinkEntity twinLinkEntity : newTwinLinks) {
            if (storedLinksMap == null) {  // no links remains in storageLinks
                twinChangesCollector.add(twinLinkEntity);
                twinChangesCollector.getHistoryCollector().add(historyService.linkCreated(twinLinkEntity));
            } else {
                TwinLinkEntity dbTwinLink = MapUtils.pullAny(storedLinksMap);
                if (dbTwinLink != null) {
                    log.warn(dbTwinLink.logShort() + " will be updated");
                    twinLinkEntity.setId(dbTwinLink.getId());
                    twinChangesCollector.add(twinLinkEntity);
                    twinChangesCollector.getHistoryCollector().add(historyService.linkUpdated(twinLinkEntity, dbTwinLink.getDstTwin(), linkDirection == LinkService.LinkDirection.forward));
                } else {
                    twinChangesCollector.add(twinLinkEntity);
                    twinChangesCollector.getHistoryCollector().add(historyService.linkCreated(twinLinkEntity));
                }
            }
        }
        deleteOutOfDateLinks(twinChangesCollector, storedLinksMap);
    }

    public void deleteOutOfDateLinks(TwinChangesCollector twinChangesCollector, Map<UUID, TwinLinkEntity> outOfDateStoredLinksMap) {
        if (outOfDateStoredLinksMap != null && CollectionUtils.isNotEmpty(outOfDateStoredLinksMap.entrySet())) { // old values must be deleted
            List<UUID> deleteIdList = new ArrayList<>();
            for (TwinLinkEntity twinLinkEntity : outOfDateStoredLinksMap.values()) {
                deleteIdList.add(twinLinkEntity.getId());
                twinChangesCollector.getHistoryCollector().add(historyService.linkDeleted(twinLinkEntity));
            }
            twinChangesCollector.deleteAll(TwinLinkEntity.class, deleteIdList);
        }
    }

    @Override
    protected FieldValueLink deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twinEntity = twinField.getTwin();
        LinkEntity linkEntity = linkService.findEntitySafe(linkUUID.extract(properties));
        LinkService.LinkDirection linkDirection = linkService.detectLinkDirection(linkEntity, twinField.getTwin().getTwinClass());
        twinLinkService.loadTwinLinks(twinEntity);
        List<TwinLinkEntity> twinLinkEntityList;
        if (linkDirection == LinkService.LinkDirection.forward)
            twinLinkEntityList = twinEntity.getTwinLinks().getForwardLinks().getList().stream().filter(l -> l.getLinkId().equals(linkEntity.getId())).toList();
        else
            twinLinkEntityList = twinEntity.getTwinLinks().getBackwardLinks().getList().stream().filter(l -> l.getLinkId().equals(linkEntity.getId())).toList();
        return new FieldValueLink(twinField.getTwinClassField(), CollectionUtils.isNotEmpty(twinLinkEntityList))
                .setTwinLinks(twinLinkEntityList)
                .setForwardLink(linkDirection == LinkService.LinkDirection.forward);
    }
}
