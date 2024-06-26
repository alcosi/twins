package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionAliasEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionCreateRqDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinflowTransitionCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinflowTransitionCreateRqDTOv1, TwinflowTransitionEntity> {
    final I18nService i18nService;

    @Override
    public void map(TwinflowTransitionCreateRqDTOv1 src, TwinflowTransitionEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setSrcTwinStatusId(src.getSrcStatusId())
                .setDstTwinStatusId(src.getDstStatusId())
                .setPermissionId(src.getPermissionId())
                .setTwinflowId(src.getTwinflowId())
                .setTwinflowTransitionAlias(new TwinflowTransitionAliasEntity().setAlias(src.getAlias()));
    }
}
