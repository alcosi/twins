package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import org.twins.core.domain.TwinClassUpdate;
import org.twins.core.dto.rest.twinclass.TwinClassUpdateRqDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.common.BasicUpdateOperationRestDTOReverseMapper;
import org.twins.core.mappers.rest.i18n.I18nRestDTOReverseMapper;
import org.twins.core.service.twinclass.TwinClassService;


@Component
@RequiredArgsConstructor
public class TwinClassUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassUpdateRqDTOv1, TwinClassUpdate> {
    final TwinClassService twinClassService;
    final BasicUpdateOperationRestDTOReverseMapper basicUpdateOperationRestDTOReverseMapper;
    final I18nRestDTOReverseMapper i18nRestDTOReverseMapper;

    @Override
    public void map(TwinClassUpdateRqDTOv1 src, TwinClassUpdate dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey() != null ? src.getKey().toUpperCase() : null)
                .setAbstractt(BooleanUtils.isTrue(src.getAbstractClass()))
                .setAliasSpace(BooleanUtils.isTrue(src.getAliasSpace()))
                .setPermissionSchemaSpace(BooleanUtils.isTrue(src.getPermissionSchemaSpace()))
                .setTwinClassSchemaSpace(BooleanUtils.isTrue(src.getTwinClassSchemaSpace()))
                .setTwinflowSchemaSpace(BooleanUtils.isTrue(src.getTwinflowSchemaSpace()))
                .setHeadHunterFeaturerId(src.getHeadHunterFeaturerId())
                .setHeadHunterParams(src.getHeadHunterParams())
                .setLogo(src.getLogo())
                .setViewPermissionId(src.getViewPermissionId())

                .setNameI18n(i18nRestDTOReverseMapper.convert(src.getNameI18n(), mapperContext))
                .setDescriptionI18n(i18nRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext))

                .setDbTwinClassEntity(twinClassService.findEntitySafe(src.getTwinClassId()))
                .setMarkerDataListUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getMarkerDataListUpdate()))
                .setTagDataListUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getTagDataListChange()))
                .setExtendsTwinClassUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getExtendsTwinClassUpdate()))
                .setHeadTwinClassUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getHeadTwinClassUpdate()));
    }
}
