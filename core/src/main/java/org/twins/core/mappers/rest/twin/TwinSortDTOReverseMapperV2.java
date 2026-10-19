package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.twin.TwinSortDTOv1;
import org.twins.core.enums.SortDirection;
import org.twins.core.mappers.rest.RestSimpleDTOConverter;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.*;

@Component
@RequiredArgsConstructor
public class TwinSortDTOReverseMapperV2 extends RestSimpleDTOConverter<List<TwinSortDTOv1>, Map<UUID, SortDirection>> {
    @Override
    public Map<UUID, SortDirection> convert(List<TwinSortDTOv1> src, MapperContext mapperContext) throws Exception {
        if (src == null) {
            return Collections.emptyMap();
        }
        var dst = new LinkedHashMap<UUID, SortDirection>();
        for (var dto : src) {
            dst.put(dto.getTwinClassFieldId(), dto.getDirection());
        }
        return dst;
    }

}
