package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldCreateRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldSaveRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinClassFieldSaveRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldSaveRqDTOv1, TwinClassFieldEntity> {

    @Override
    public void map(TwinClassFieldSaveRqDTOv1 src, TwinClassFieldEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setViewPermissionId(src.getViewPermissionId())
                .setEditPermissionId(src.getEditPermissionId())
                .setRequired(src.getRequired())
                .setFieldTyperFeaturerId(src.getFieldTyperFeaturerId())
                .setFieldTyperParams(src.getFieldTyperParams())
        ;
    }
}
