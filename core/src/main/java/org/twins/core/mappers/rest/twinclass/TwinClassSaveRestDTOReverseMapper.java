package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.twinclass.TwinClassSave;
import org.twins.core.dto.rest.twinclass.TwinClassSaveRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Deprecated
@Component
@RequiredArgsConstructor
public class TwinClassSaveRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassSaveRqDTOv1, TwinClassSave> {
    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassSaveRqDTOv1 src, TwinClassSave dst, MapperContext mapperContext) throws Exception {
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
                        .setViewPermissionId(src.getViewPermissionId())
                        .setCreatePermissionId(src.getCreatePermissionId())
                        .setEditPermissionId(src.getEditPermissionId())
                        .setDeletePermissionId(src.getDeletePermissionId())
                        .setOwnerType(src.getOwnerType())
                        .setAssigneeRequired(src.getAssigneeRequired())
                        .setSegment(src.getSegment())
                        .setExternalId(src.getExternalId())
                        .setExternalProperties(src.getExternalProperties())
                        .setExternalJson(src.getExternalJson())
                        .setTwinClassFreezeId(src.getTwinClassFreezeId())
                );
    }
}
