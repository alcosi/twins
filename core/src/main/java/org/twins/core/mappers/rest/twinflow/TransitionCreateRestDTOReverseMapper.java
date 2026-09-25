package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionAliasEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.domain.twinflow.TransitionCreate;
import org.twins.core.dto.rest.twinflow.TransitionCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class TransitionCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TransitionCreateDTOv1, TransitionCreate> {

    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;

    @Override
    public void map(TransitionCreateDTOv1 src, TransitionCreate dst, MapperContext mapperContext) throws Exception {
        I18nEntity nameI18n = i18nSaveRestDTOReverseMapper.convert(src.getNameI18n(), mapperContext);
        I18nEntity descriptionI18n = i18nSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext);
        TwinflowTransitionEntity entity = new TwinflowTransitionEntity()
                .setSrcTwinStatusId(src.getSrcStatusId())
                .setDstTwinStatusId(src.getDstStatusId())
                .setPermissionId(src.getPermissionId())
                .setTwinflowId(src.getTwinflowId())
                .setInbuiltTwinFactoryId(src.getInbuiltTwinFactoryId())
                .setDraftingTwinFactoryId(src.getDraftingTwinFactoryId())
                .setTwinflowTransitionTypeId(src.getTwinflowTransitionTypeId())
                .setTwinflowTransitionAlias(new TwinflowTransitionAliasEntity().setAlias(src.getAlias()));
        dst
                .setEntity(entity)
                .setNameI18n(nameI18n)
                .setDescriptionI18n(descriptionI18n);
    }
}
