package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAliasEntity;
import org.twins.core.enums.twin.TwinAliasType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinAliasMode;
import org.twins.core.service.twin.TwinAliasService;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class TwinAliasRestDTOMapper extends RestSimpleDTOMapper<TwinEntity, Set<String>> {
    private final TwinAliasService twinAliasService;

    @Override
    public Set<String> convert(TwinEntity src,  MapperContext mapperContext) {
        Set<String> result = new HashSet<>();
        twinAliasService.loadAliases(src);
        if (mapperContext.getModeOrUse(TwinAliasMode.HIDE) == TwinAliasMode.ALL)
            return src.getTwinAliases().stream().map(TwinAliasEntity::getAlias).collect(Collectors.toSet());
        TwinAliasType twinAliasType = null;
        switch (mapperContext.getModeOrUse(TwinAliasMode.HIDE)) {
            case B:
                twinAliasType =  TwinAliasType.B;
                break;
            case C:
                twinAliasType =  TwinAliasType.C;
                break;
            case D:
                twinAliasType =  TwinAliasType.D;
                break;
            case K:
                twinAliasType =  TwinAliasType.K;
                break;
            case S:
                twinAliasType =  TwinAliasType.S;
                break;
            case T:
                twinAliasType =  TwinAliasType.T;
                break;
            case HIDE:
                return result;
        }
        TwinAliasEntity twinAliasEntity = src.getTwinAliases().get(twinAliasType);
        if (twinAliasEntity != null)
            result.add(twinAliasEntity.getAlias());
        return result;
    }


    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinAliasMode.HIDE);
    }

    @Override
    public void map(TwinEntity src, Set<String> dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }
}
