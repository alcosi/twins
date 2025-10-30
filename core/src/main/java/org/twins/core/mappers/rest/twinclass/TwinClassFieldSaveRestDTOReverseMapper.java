package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.twinclass.TwinClassFieldSave;
import org.twins.core.dto.rest.twinclass.TwinClassFieldSaveRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Deprecated
@Component
@RequiredArgsConstructor
public class TwinClassFieldSaveRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldSaveRqDTOv1, TwinClassFieldSave> {
    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassFieldSaveRqDTOv1 src, TwinClassFieldSave dst, MapperContext mapperContext) throws Exception {
        dst
                .setNameI18n(i18nSaveRestDTOReverseMapper.convert(src.getNameI18n()))
                .setDescriptionI18n(i18nSaveRestDTOReverseMapper.convert(src.getDescriptionI18n()))
                .setField(new TwinClassFieldEntity()
                        .setKey(src.getKey())
                        .setViewPermissionId(src.getViewPermissionId())
                        .setEditPermissionId(src.getEditPermissionId())
                        .setRequired(src.getRequired())
                        .setSystem(src.getSystem())
                        .setFieldTyperFeaturerId(src.getFieldTyperFeaturerId())
                        .setFieldTyperParams(src.getFieldTyperParams())
                        .setTwinSorterFeaturerId(src.getTwinSorterFeaturerId())
                        .setTwinSorterParams(src.getTwinSorterParams())
                        .setExternalId(src.getExternalId())
                        .setExternalProperties(src.getExternalProperties())
                        .setOrder(src.getOrder()));
    }
}
