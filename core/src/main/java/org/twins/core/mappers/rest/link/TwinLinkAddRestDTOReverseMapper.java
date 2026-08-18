package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.MapUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twin.TwinFieldValueRestDTOReverseMapperV2;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.link.LinkService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class TwinLinkAddRestDTOReverseMapper extends RestSimpleDTOMapper<TwinLinkAddDTOv1, TwinLinkEntity> {
    /** MapperContext cache prefix under which links are preloaded per batch (no N+1). */
    protected static final String RELATION_TWIN_LINK_CACHE_PREFIX = "relationTwinLink:";

    private final AuthService authService;
    private final LinkService linkService;
    private final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;

    @Override
    public void map(TwinLinkAddDTOv1 src, TwinLinkEntity dst, MapperContext mapperContext) throws Exception {
        ApiUser apiUser = authService.getApiUser();
        dst
                .setLinkId(src.getLinkId())
                .setDstTwinId(src.getDstTwinId())
                .setCreatedByUserId(apiUser.getUser().getId());
        // Relation twin initial fields (relation attributes): converted HERE, at the mapper layer —
        // the same call and pattern as TwinCreateRqRestDTOReverseMapper does for twin fields.
        if (MapUtils.isNotEmpty(src.getRelationTwinFields())) {
            LinkEntity link = findLink(src.getLinkId(), mapperContext);
            if (link.getRelationTwinClassId() == null)
                throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT,
                        "relationTwinFields provided but " + link.logShort() + " has no relation_twin_class_id");
            dst.setRelationTwinFields(twinFieldValueRestDTOReverseMapperV2
                    .mapFields(link.getRelationTwinClassId(), src.getRelationTwinFields()));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinLinkAddDTOv1> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        // batch: ONE query for all links referenced with relationTwinFields — no per-DTO link lookups
        Set<UUID> linkIds = new HashSet<>();
        for (TwinLinkAddDTOv1 dto : srcCollection)
            if (dto.getRelationTwinFields() != null && !dto.getRelationTwinFields().isEmpty() && dto.getLinkId() != null)
                linkIds.add(dto.getLinkId());
        if (linkIds.isEmpty())
            return;
        for (LinkEntity link : linkService.findEntitiesSafe(linkIds))
            mapperContext.putToCache(LinkEntity.class, RELATION_TWIN_LINK_CACHE_PREFIX + link.getId(), link);
    }


    private LinkEntity findLink(UUID linkId, MapperContext mapperContext) throws ServiceException {
        LinkEntity cached = mapperContext.getFromCache(LinkEntity.class, RELATION_TWIN_LINK_CACHE_PREFIX + linkId);
        if (cached != null)
            return cached;
        return linkService.findEntitySafe(linkId); // single-item convert() path — no batch to leverage
    }
}
