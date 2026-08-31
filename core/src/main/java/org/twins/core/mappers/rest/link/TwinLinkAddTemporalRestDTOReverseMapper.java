package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.MapUtils;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.twinlink.TwinLinkCreate;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv2;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.auth.AuthService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class TwinLinkAddTemporalRestDTOReverseMapper extends RestSimpleDTOMapper<TwinLinkAddDTOv2, TwinLinkCreate> {
    private final AuthService authService;
    private final RelationTwinFieldsConverter relationTwinFieldsConverter;

    @Override
    public void map(TwinLinkAddDTOv2 src, TwinLinkCreate dst, MapperContext mapperContext) throws Exception {
        ApiUser apiUser = authService.getApiUser();
        TwinLinkEntity twinLink = new TwinLinkEntity()
                .setLinkId(src.getLinkId())
                .setCreatedByUserId(apiUser.getUser().getId());
        if (StringUtils.isBlank(src.getDstTwinId()))
            throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT,
                    "Missed dstTwinId");
        try {
            UUID dstTwinId = UUID.fromString(src.getDstTwinId());
            twinLink.setDstTwinId(dstTwinId);
        } catch (IllegalArgumentException e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT,
                    "DstTwinId can not be parsed to UUID");
        }
        dst.setTwinLink(twinLink);
        // Relation twin initial fields (relation attributes): same mapper-layer conversion as the v1 add mapper
        if (MapUtils.isNotEmpty(src.getRelationTwinFields()))
            dst.setRelationTwinFields(relationTwinFieldsConverter.convert(src.getLinkId(), src.getRelationTwinFields(), mapperContext));
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinLinkAddDTOv2> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        // batch: ONE query for all links referenced with relationTwinFields — no per-DTO link lookups
        Set<UUID> linkIds = new HashSet<>();
        for (TwinLinkAddDTOv2 dto : srcCollection)
            if (MapUtils.isNotEmpty(dto.getRelationTwinFields()) && dto.getLinkId() != null)
                linkIds.add(dto.getLinkId());
        if (!linkIds.isEmpty())
            relationTwinFieldsConverter.preloadLinks(linkIds, mapperContext);
    }
}
