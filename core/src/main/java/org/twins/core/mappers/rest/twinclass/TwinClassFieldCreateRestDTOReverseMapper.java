package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.twinclass.TwinClassFieldSave;
import org.twins.core.dto.rest.twinclass.TwinClassFieldCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Deprecated
@Component
@RequiredArgsConstructor
public class TwinClassFieldCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldCreateRqDTOv1, TwinClassFieldSave> {
    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassFieldCreateRqDTOv1 src, TwinClassFieldSave dst, MapperContext mapperContext) throws Exception {
        dst
                .setNameI18n(i18nSaveRestDTOReverseMapper.convert(src.getNameI18n()))
                .setDescriptionI18n(i18nSaveRestDTOReverseMapper.convert(src.getDescriptionI18n()))
                .setField(new TwinClassFieldEntity()
                        .setKey(src.getKey())
                        .setViewPermissionId(src.getViewPermissionId())
                        .setEditPermissionId(src.getEditPermissionId())
                        .setRequired(src.getRequired())
                        .setSystem(src.getSystem())
                        .setInheritable(src.getInheritable())
                        .setFieldTyperFeaturerId(src.getFieldTyperFeaturerId())
                        .setFieldTyperParams(src.getFieldTyperParams())
                        .setTwinSorterFeaturerId(src.getTwinSorterFeaturerId())
                        .setTwinSorterParams(src.getTwinSorterParams())
                        .setExternalId(src.getExternalId())
                        .setExternalProperties(src.getExternalProperties())
                        .setOrder(src.getOrder())
                        .setTwinClassId(src.getTwinClassId()));
    }
}
