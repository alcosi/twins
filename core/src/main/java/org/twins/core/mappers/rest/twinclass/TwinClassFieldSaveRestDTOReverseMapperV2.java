package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldSave;
import org.twins.core.dto.rest.twinclass.TwinClassFieldSaveDTOv1;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinClassFieldSaveRestDTOReverseMapperV2 extends RestSimpleDTOMapper<TwinClassFieldSaveDTOv1, TwinClassFieldSave> {
    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassFieldSaveDTOv1 src, TwinClassFieldSave dst, MapperContext mapperContext) throws Exception {
        dst
                .setNameI18n(i18nSaveRestDTOReverseMapper.convert(src.getNameI18n()))
                .setDescriptionI18n(i18nSaveRestDTOReverseMapper.convert(src.getDescriptionI18n()))
                .setField(new TwinClassFieldEntity()
                        .setKey(src.getKey())
                        .setViewPermissionId(src.getViewPermissionId())
                        .setEditPermissionId(src.getEditPermissionId())
                        .setRequired(src.getRequired())
                        .setFieldTyperFeaturerId(src.getFieldTyperFeaturerId())
                        .setFieldTyperParams(src.getFieldTyperParams())
                        .setExternalId(src.getExternalId()));

    }
}
