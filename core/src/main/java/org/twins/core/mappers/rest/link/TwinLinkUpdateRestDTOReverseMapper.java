package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.MapUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.twinlink.TwinLinkUpdate;
import org.twins.core.dto.rest.link.TwinLinkUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class TwinLinkUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinLinkUpdateDTOv1, TwinLinkUpdate> {

    private final TwinService twinService;
    private final RelationTwinFieldsConverter relationTwinFieldsConverter;

    @Override
    public void map(TwinLinkUpdateDTOv1 src, TwinLinkUpdate dst, MapperContext mapperContext) throws Exception {
        dst.setTwinLink(new TwinLinkEntity()
                .setDstTwin(twinService.findEntitySafe(src.getDstTwinId()))
                .setId(src.getId())
                .setDstTwinId(src.getDstTwinId())); // also it can be srcTwinId for backward link. it must be changed in service
        // Relation twin field updates (relation attributes): converted HERE, at the mapper layer —
        // resolved against the relation twin itself (its id equals the twin_link id by ID equality).
        if (MapUtils.isNotEmpty(src.getRelationTwinFields()))
            dst.setRelationTwinFields(relationTwinFieldsConverter.convertForRelationTwin(src.getId(), src.getRelationTwinFields(), mapperContext));
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinLinkUpdateDTOv1> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        // batch: ONE query for all relation twins referenced with relationTwinFields — no per-DTO lookups
        Set<UUID> twinLinkIds = new HashSet<>();
        for (TwinLinkUpdateDTOv1 dto : srcCollection)
            if (MapUtils.isNotEmpty(dto.getRelationTwinFields()) && dto.getId() != null)
                twinLinkIds.add(dto.getId());
        if (!twinLinkIds.isEmpty())
            relationTwinFieldsConverter.preloadRelationTwins(twinLinkIds, mapperContext);
    }
}
