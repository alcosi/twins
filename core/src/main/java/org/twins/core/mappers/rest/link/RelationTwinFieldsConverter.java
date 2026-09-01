package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twin.TwinFieldValueRestDTOReverseMapperV2;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Shared mapper-layer conversion of initial relation-twin field values (relation attributes):
 * Map&lt;String,String&gt; -> List&lt;FieldValue&gt; via TwinFieldValueRestDTOReverseMapperV2.mapFields —
 * the same conversion entry TwinCreateRqRestDTOReverseMapper uses for twin fields. The link lookup is
 * batched through the MapperContext cache (ONE query per converted collection — no N+1), with a
 * single-item fallback when no batch was preloaded.
 * Used by both TwinLinkAddRestDTOReverseMapper (v1) and TwinLinkAddTemporalRestDTOReverseMapper (v2).
 */
@Component
@RequiredArgsConstructor
public class RelationTwinFieldsConverter {
    /** MapperContext cache prefixes under which links / relation twins are preloaded per batch (no N+1). */
    protected static final String RELATION_TWIN_LINK_CACHE_PREFIX = "relationTwinLink:";
    protected static final String RELATION_TWIN_CACHE_PREFIX = "relationTwin:";

    private final LinkService linkService;
    private final TwinService twinService;
    private final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;

    /** Create path: resolves the link by id, validates its relation_twin_class_id, converts the fields. */
    public List<FieldValue> convert(UUID linkId, Map<String, String> relationTwinFields, MapperContext mapperContext) throws ServiceException {
        LinkEntity link = mapperContext.getFromCache(LinkEntity.class, RELATION_TWIN_LINK_CACHE_PREFIX + linkId);
        if (link == null)
            link = linkService.findEntitySafe(linkId); // single-item convert() path — no batch to leverage
        if (link.getRelationTwinClassId() == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT,
                    "relationTwinFields provided but " + link.logShort() + " has no relation_twin_class_id");
        return mapFields(link.logShort(), link.getRelationTwinClassId(), relationTwinFields);
    }

    /** batch for the create path: ONE query for all links referenced with relationTwinFields. */
    public void preloadLinks(Collection<UUID> linkIds, MapperContext mapperContext) throws ServiceException {
        if (CollectionUtils.isEmpty(linkIds))
            return;
        for (LinkEntity link : linkService.findEntitiesSafe(linkIds))
            mapperContext.putToCache(LinkEntity.class, RELATION_TWIN_LINK_CACHE_PREFIX + link.getId(), link);
    }

    /**
     * Update path: resolves the RELATION TWIN itself — its id equals the twin_link id (ID equality) —
     * and converts the fields against the twin's own class (which is link.relation_twin_class_id).
     */
    public List<FieldValue> convertForRelationTwin(UUID twinLinkId, Map<String, String> relationTwinFields, MapperContext mapperContext) throws ServiceException {
        TwinEntity relationTwin = mapperContext.getFromCache(TwinEntity.class, RELATION_TWIN_CACHE_PREFIX + twinLinkId);
        if (relationTwin == null) { // single-item convert() path — no batch to leverage
            try {
                relationTwin = twinService.findEntitySafe(twinLinkId);
            } catch (ServiceException e) {
                throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT,
                        "relationTwinFields provided but twin_link[" + twinLinkId + "] has no relation twin", e);
            }
        }
        return mapFields(relationTwin.logShort(), relationTwin.getTwinClassId(), relationTwinFields);
    }

    /** batch for the update path: ONE query for all relation twins referenced with relationTwinFields. */
    public void preloadRelationTwins(Collection<UUID> twinIds, MapperContext mapperContext) throws ServiceException {
        if (CollectionUtils.isEmpty(twinIds))
            return;
        for (TwinEntity relationTwin : twinService.findEntitiesSafe(twinIds))
            mapperContext.putToCache(TwinEntity.class, RELATION_TWIN_CACHE_PREFIX + relationTwin.getId(), relationTwin);
    }

    protected List<FieldValue> mapFields(String contextLog, UUID twinClassId, Map<String, String> relationTwinFields) throws ServiceException {
        try {
            return twinFieldValueRestDTOReverseMapperV2.mapFields(twinClassId, relationTwinFields);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    "Failed to convert relationTwinFields for " + contextLog + ": " + e.getMessage(), e);
        }
    }
}
