package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassCreateRqDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinClassCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassCreateRqDTOv1, TwinClassEntity> {
    final I18nService i18nService;

    @Override
    public void map(TwinClassCreateRqDTOv1 src, TwinClassEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setAbstractt(src.isAbstractClass())
                .setExtendsTwinClassId(src.getExtendsTwinClassId())
                .setHeadTwinClassId(src.getHeadTwinClassId())
                .setAliasSpace(src.isAliasSpace())
                .setPermissionSchemaSpace(src.isPermissionSchemaSpace())
                .setTwinClassSchemaSpace(src.isTwinClassSchemaSpace())
                .setTwinflowSchemaSpace(src.isTwinflowSchemaSpace())
                .setHeadTwinClassId(src.getHeadTwinClassId())
                .setMarkerDataListId(src.getMarkerDataListId())
                .setTagDataListId(src.getTagDataListId())
                .setLogo(src.getLogo())
                .setViewPermissionId(src.getViewPermissionId())
        ;
    }
}
