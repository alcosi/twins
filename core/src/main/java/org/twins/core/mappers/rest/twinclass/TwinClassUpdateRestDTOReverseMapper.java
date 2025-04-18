package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassUpdate;
import org.twins.core.dto.rest.twinclass.TwinClassUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.common.BasicUpdateOperationRestDTOReverseMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.twinclass.TwinClassService;


@Component
@RequiredArgsConstructor
public class TwinClassUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassUpdateRqDTOv1, TwinClassUpdate> {

    private final BasicUpdateOperationRestDTOReverseMapper basicUpdateOperationRestDTOReverseMapper;
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;

    private final TwinClassService twinClassService;

    @Override
    public void map(TwinClassUpdateRqDTOv1 src, TwinClassUpdate dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey() != null ? src.getKey().toUpperCase() : null)
                .setAbstractt(src.getAbstractClass())
                .setAliasSpace(src.getAliasSpace())
                .setPermissionSchemaSpace(src.getPermissionSchemaSpace())
                .setTwinClassSchemaSpace(src.getTwinClassSchemaSpace())
                .setTwinflowSchemaSpace(src.getTwinflowSchemaSpace())
                .setHeadHunterFeaturerId(src.getHeadHunterFeaturerId())
                .setHeadHunterParams(src.getHeadHunterParams())
                .setLogo(src.getLogo())
                .setViewPermissionId(src.getViewPermissionId())
                .setEditPermissionId(src.getEditPermissionId())
                .setCreatePermissionId(src.getCreatePermissionId())
                .setDeletePermissionId(src.getDeletePermissionId())
                .setAssigneeRequired(src.getAssigneeRequired())

                .setNameI18n(i18NSaveRestDTOReverseMapper.convert(src.getNameI18n(), mapperContext))
                .setDescriptionI18n(i18NSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext))

                .setDbTwinClassEntity(twinClassService.findEntitySafe(src.getTwinClassId()))
                .setMarkerDataListUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getMarkerDataListUpdate()))
                .setTagDataListUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getTagDataListUpdate()))
                .setExtendsTwinClassUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getExtendsTwinClassUpdate()))
                .setHeadTwinClassUpdate(basicUpdateOperationRestDTOReverseMapper.convert(src.getHeadTwinClassUpdate()));
    }
}
