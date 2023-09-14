package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAliasEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dto.rest.twin.TwinCreateRsDTOv1;
import org.twins.core.dto.rest.twin.TwinDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;
import org.twins.core.service.twin.TwinService;

import java.util.ArrayList;
import java.util.List;


@Component
@RequiredArgsConstructor
public class TwinCreateRsRestDTOMapper extends RestSimpleDTOMapper<TwinService.TwinCreateResult, TwinCreateRsDTOv1> {

    @Override
    public void map(TwinService.TwinCreateResult src, TwinCreateRsDTOv1 dst, MapperProperties mapperProperties) throws Exception {
        List<String> aliasList = new ArrayList<>();
        for (TwinAliasEntity twinAliasEntity : src.getAliasEntityList())
            aliasList.add(twinAliasEntity.getAlias());
        dst
                .setTwinId(src.getCreatedTwin().id())
                .setAliasList(aliasList);
    }
}
