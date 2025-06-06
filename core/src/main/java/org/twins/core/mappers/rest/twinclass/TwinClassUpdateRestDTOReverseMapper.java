package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.twinclass.TwinClassUpdate;
import org.twins.core.dto.rest.twinclass.TwinClassUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.common.BasicUpdateOperationRestDTOReverseMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.twinclass.TwinClassService;

@Deprecated
@Component
@RequiredArgsConstructor
public class TwinClassUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassUpdateRqDTOv1, TwinClassUpdate> {
    private final TwinClassSaveRestDTOReverseMapper twinClassSaveRestDTOReverseMapper;
    private final BasicUpdateOperationRestDTOReverseMapper basicUpdateOperationRestDTOReverseMapper;

    @Override
    public void map(TwinClassUpdateRqDTOv1 src, TwinClassUpdate dst, MapperContext mapperContext) throws Exception {
        twinClassSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setMarkerDataListUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getMarkerDataListUpdate()))
                .setTagDataListUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getTagDataListUpdate()))
                .setExtendsTwinClassUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getExtendsTwinClassUpdate()))
                .setHeadTwinClassUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getHeadTwinClassUpdate()))
                .getTwinClass().setId(src.getTwinClassId());
    }
}
