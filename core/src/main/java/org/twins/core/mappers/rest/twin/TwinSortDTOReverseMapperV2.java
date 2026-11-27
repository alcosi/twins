package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.twin.TwinSortDTOv1;
import org.twins.core.mappers.rest.SortDTOReverseMapper;

import java.util.UUID;

/**
 * Twin sorts by twin class field id (not by an enum) — the map key is the twinClassFieldId,
 * consumed by {@code TwinSearchService.createSortSpecification(UUID, SortDirection, Locale)}.
 */
@Component
@RequiredArgsConstructor
public class TwinSortDTOReverseMapperV2 extends SortDTOReverseMapper<TwinSortDTOv1, UUID> {
    @Override
    protected UUID sortField(TwinSortDTOv1 dto) {
        return dto.getTwinClassFieldId();
    }
}
