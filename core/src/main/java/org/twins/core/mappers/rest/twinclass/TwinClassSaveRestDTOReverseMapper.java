package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassSaveRqDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinClassSaveRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassSaveRqDTOv1, TwinClassEntity> {
    final I18nService i18nService;

    @Override
    public void map(TwinClassSaveRqDTOv1 src, TwinClassEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setAbstractt(BooleanUtils.isTrue(src.getAbstractClass()))
                .setAliasSpace(BooleanUtils.isTrue(src.getAliasSpace()))
                .setPermissionSchemaSpace(BooleanUtils.isTrue(src.getPermissionSchemaSpace()))
                .setTwinClassSchemaSpace(BooleanUtils.isTrue(src.getTwinClassSchemaSpace()))
                .setTwinflowSchemaSpace(BooleanUtils.isTrue(src.getTwinflowSchemaSpace()))
                .setHeadHunterFeaturerId(src.getHeadHunterFeaturerId())
                .setHeadHunterParams(src.getHeadHunterParams())
                .setLogo(src.getLogo())
                .setViewPermissionId(src.getViewPermissionId())
        ;
    }
}
