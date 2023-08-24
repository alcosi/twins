package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.mappers.rest.RestDTOConverter;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TwinClassListRestDTOMapper implements RestDTOConverter<List<TwinClassEntity>, List<TwinClassDTOv1>> {
    final TwinClassRestDTOMapper twinClassRestDTOMapper;

    public List<TwinClassDTOv1> convert(List<TwinClassEntity> twinClassEntityList) {
        List<TwinClassDTOv1> ret = new ArrayList<>();
        for (TwinClassEntity entity : twinClassEntityList) {
            ret.add(twinClassRestDTOMapper.convert(entity));
        }
        return ret;
    }
}
