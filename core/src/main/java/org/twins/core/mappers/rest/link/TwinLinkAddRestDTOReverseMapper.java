package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.MapUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.twinlink.TwinLinkCreate;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.auth.AuthService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class TwinLinkAddRestDTOReverseMapper extends RestSimpleDTOMapper<TwinLinkAddDTOv1, TwinLinkCreate> {
    private final AuthService authService;
    private final RelationTwinFieldsConverter relationTwinFieldsConverter;

    @Override
    public void map(TwinLinkAddDTOv1 src, TwinLinkCreate dst, MapperContext mapperContext) throws Exception {
        ApiUser apiUser = authService.getApiUser();
        dst.setTwinLink(new TwinLinkEntity()
                .setLinkId(src.getLinkId())
                .setDstTwinId(src.getDstTwinId())
                .setCreatedByUserId(apiUser.getUser().getId()));
        // Relation twin initial fields (relation attributes): converted HERE, at the mapper layer —
        // the same call and pattern as TwinCreateRqRestDTOReverseMapper does for twin fields.
        if (MapUtils.isNotEmpty(src.getRelationTwinFields()))
            dst.setRelationTwinFields(relationTwinFieldsConverter.convert(src.getLinkId(), src.getRelationTwinFields(), mapperContext));
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinLinkAddDTOv1> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        // batch: ONE query for all links referenced with relationTwinFields — no per-DTO link lookups
        Set<UUID> linkIds = new HashSet<>();
        for (TwinLinkAddDTOv1 dto : srcCollection)
            if (MapUtils.isNotEmpty(dto.getRelationTwinFields()) && dto.getLinkId() != null)
                linkIds.add(dto.getLinkId());
        if (!linkIds.isEmpty())
            relationTwinFieldsConverter.preloadLinks(linkIds, mapperContext);
    }
}
