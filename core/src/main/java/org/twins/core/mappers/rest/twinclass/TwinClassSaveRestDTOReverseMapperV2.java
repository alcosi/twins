package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassSave;
import org.twins.core.dto.rest.twinclass.TwinClassSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassSaveRestDTOReverseMapperV2 extends RestSimpleDTOMapper<TwinClassSaveDTOv1, TwinClassSave> {
    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassSaveDTOv1 src, TwinClassSave dst, MapperContext mapperContext) throws Exception {
        dst
                .setNameI18n(i18nSaveRestDTOReverseMapper.convert(src.getNameI18n()))
                .setDescriptionI18n(i18nSaveRestDTOReverseMapper.convert(src.getDescriptionI18n()))
                .setTwinClass(new TwinClassEntity()
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
                        .setCreatePermissionId(src.getCreatePermissionId())
                        .setEditPermissionId(src.getEditPermissionId())
                        .setDeletePermissionId(src.getDeletePermissionId())
                        .setOwnerType(src.getOwnerType())
                        .setAssigneeRequired(src.getAssigneeRequired())
                        .setExternalId(src.getExternalId())
                        .setExternalProperties(src.getExternalProperties())
                );
    }
}
