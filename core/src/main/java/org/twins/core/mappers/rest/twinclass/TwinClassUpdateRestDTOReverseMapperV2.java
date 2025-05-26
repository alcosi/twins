package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassUpdate;
import org.twins.core.dto.rest.twinclass.TwinClassUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.common.BasicUpdateOperationRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassUpdateRestDTOReverseMapperV2 extends RestSimpleDTOMapper<TwinClassUpdateDTOv1, TwinClassUpdate> {
    private final TwinClassSaveRestDTOReverseMapperV2 twinClassSaveRestDTOReverseMapper;
    private final BasicUpdateOperationRestDTOReverseMapper basicUpdateOperationRestDTOReverseMapper;

    @Override
    public void map(TwinClassUpdateDTOv1 src, TwinClassUpdate dst, MapperContext mapperContext) throws Exception {
        twinClassSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setMarkerDataListUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getMarkerDataListUpdate()))
                .setTagDataListUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getTagDataListUpdate()))
                .setExtendsTwinClassUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getExtendsTwinClassUpdate()))
                .setHeadTwinClassUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getHeadTwinClassUpdate()))
                .getTwinClass().setId(src.getTwinClassId());
    }
}