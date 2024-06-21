package org.twins.core.mappers.rest.twin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAliasEntity;
import org.twins.core.dto.rest.twin.TwinAliasDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinAliasRestDTOMapper extends RestSimpleDTOMapper<TwinAliasEntity, TwinAliasDTOv1> {
    final I18nService i18nService;

    @Override
    public void map(TwinAliasEntity src,  TwinAliasDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(TwinAliasRestDTOMapper.Mode.DETAILED)) {
            case DETAILED:
                dst
                        .id(src.getId())
                        .alias(src.getAlias())
                        .twinId(src.getTwinId())
                        .domainId(src.getDomainId())
                        .businessAccountId(src.getBusinessAccountId())
                        .userId(src.getUserId());
                break;
            case SHORT:
                dst
                        .id(src.getId())
                        .alias(src.getAlias());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(Mode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinAliasEntity src) {
        return src.getId().toString();
    }

    @AllArgsConstructor
    public enum Mode implements MapperMode {
        HIDE(0),
        SHORT(1),
        DETAILED(2);

        public static final String _HIDE = "HIDE";
        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";

        @Getter
        final int priority;
    }
}
