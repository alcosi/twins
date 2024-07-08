package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAliasEntity;
import org.twins.core.dto.rest.twin.TwinAliasDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.TwinAliasMode;


@Component
@RequiredArgsConstructor
public class TwinAliasRestDTOMapper extends RestSimpleDTOMapper<TwinAliasEntity, TwinAliasDTOv1> {

    @Override
    public void map(TwinAliasEntity src,  TwinAliasDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(TwinAliasMode.DETAILED)) {
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
        return mapperContext.hasModeOrEmpty(TwinAliasMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinAliasEntity src) {
        return src.getId().toString();
    }

}
