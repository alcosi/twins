package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.twinclass.TwinClassUpdate;
import org.twins.core.dto.rest.twinclass.TwinClassUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.common.BasicUpdateOperationRestDTOReverseMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassUpdateRestDTOReverseMapperV2 extends RestSimpleDTOMapper<TwinClassUpdateDTOv1, TwinClassUpdate> {
    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;
    private final BasicUpdateOperationRestDTOReverseMapper basicUpdateOperationRestDTOReverseMapper;

    @Override
    public void map(TwinClassUpdateDTOv1 src, TwinClassUpdate dst, MapperContext mapperContext) throws Exception {
        dst
                .setNameI18n(i18nSaveRestDTOReverseMapper.convert(src.getNameI18n()))
                .setDescriptionI18n(i18nSaveRestDTOReverseMapper.convert(src.getDescriptionI18n()))
                .setTwinClass(new TwinClassEntity()
                        .setKey(src.getKey())
                        .setAbstractt(BooleanUtils.isTrue(src.getAbstractClass()))
                        .setUniqueName(BooleanUtils.isTrue(src.getUniqueName()))
                        .setAliasSpace(BooleanUtils.isTrue(src.getAliasSpace()))
                        .setPermissionSchemaSpace(BooleanUtils.isTrue(src.getPermissionSchemaSpace()))
                        .setTwinClassSchemaSpace(BooleanUtils.isTrue(src.getTwinClassSchemaSpace()))
                        .setTwinflowSchemaSpace(BooleanUtils.isTrue(src.getTwinflowSchemaSpace()))
                        .setHeadHunterFeaturerId(src.getHeadHunterFeaturerId())
                        .setHeadHunterParams(src.getHeadHunterParams())
                        .setViewPermissionId(src.getViewPermissionId())
                        .setCreatePermissionId(src.getCreatePermissionId())
                        .setOwnerType(src.getOwnerType())
                        .setAssigneeRequired(src.getAssigneeRequired())
                        .setSegment(src.getSegment())
                        .setExternalId(src.getExternalId())
                        .setExternalProperties(src.getExternalProperties())
                        .setExternalJson(src.getExternalJson())
                        .setTwinClassFreezeId(src.getTwinClassFreezeId())
                );
        dst
                .setMarkerDataListUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getMarkerDataListUpdate()))
                .setTagDataListUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getTagDataListUpdate()))
                .setFlavorDataListUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getFlavorDataListUpdate()))
                .setExtendsTwinClassUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getExtendsTwinClassUpdate()))
                .setHeadTwinClassUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getHeadTwinClassUpdate()))
                .getTwinClass().setId(src.getTwinClassId());
    }
}
