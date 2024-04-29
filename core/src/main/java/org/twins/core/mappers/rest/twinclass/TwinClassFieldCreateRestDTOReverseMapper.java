package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldCreateRqDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinClassFieldCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldCreateRqDTOv1, TwinClassFieldEntity> {
    final I18nService i18nService;

    @Override
    public void map(TwinClassFieldCreateRqDTOv1 src, TwinClassFieldEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setViewPermissionId(src.getViewPermissionId())
                .setEditPermissionId(src.getEditPermissionId())
        ;
    }
}
