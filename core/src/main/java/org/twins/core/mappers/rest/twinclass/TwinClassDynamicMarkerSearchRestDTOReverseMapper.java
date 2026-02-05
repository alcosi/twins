package org.twins.core.mappers.rest.twinclass;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassDynamicMarkerSearch;
import org.twins.core.dto.rest.twinclass.TwinClassDynamicMarkerSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinClassDynamicMarkerSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassDynamicMarkerSearchDTOv1, TwinClassDynamicMarkerSearch> {

    @Override
    public void map(TwinClassDynamicMarkerSearchDTOv1 src, TwinClassDynamicMarkerSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinClassIdList(src.getTwinClassIdList())
                .setTwinClassIdExcludeList(src.getTwinClassIdExcludeList())
                .setTwinValidatorSetIdList(src.getTwinValidatorSetIdList())
                .setTwinValidatorSetIdExcludeList(src.getTwinValidatorSetIdExcludeList())
                .setMarkerDataListOptionIdList(src.getMarkerDataListOptionIdList())
                .setMarkerDataListOptionIdExcludeList(src.getMarkerDataListOptionIdExcludeList());
    }
}
