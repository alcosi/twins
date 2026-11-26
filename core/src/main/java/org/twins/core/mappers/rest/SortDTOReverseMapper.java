package org.twins.core.mappers.rest;

import org.twins.core.dto.rest.SortDTOv1;
import org.twins.core.enums.SortDirection;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base converter for the multi-field sort DTO family (see docs/dto_code_convention.md §3.3):
 * collapses a {@link SortDTOv1} list into an ordered sortField→direction map — a LinkedHashMap,
 * so the list order is preserved as ORDER BY precedence in the search pipeline.
 * Each subclass pins its entity sort DTO and sort field type.
 */
public abstract class SortDTOReverseMapper<D extends SortDTOv1, SF> extends RestSimpleDTOConverter<List<D>, Map<SF, SortDirection>> {
    @Override
    public Map<SF, SortDirection> convert(List<D> src, MapperContext mapperContext) throws Exception {
        Map<SF, SortDirection> dst = new LinkedHashMap<>();
        if (src != null) {
            for (D dto : src) {
                dst.put(sortField(dto), dto.getSortDirection());
            }
        }
        return dst;
    }

    protected abstract SF sortField(D dto);
}
