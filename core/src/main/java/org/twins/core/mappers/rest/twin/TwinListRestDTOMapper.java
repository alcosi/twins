package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinDTOv1;
import org.twins.core.mappers.rest.RestDTOConverter;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TwinListRestDTOMapper implements RestDTOConverter<List<TwinEntity>, List<TwinDTOv1>> {
    final TwinRestDTOMapper twinRestDTOMapper;

    public List<TwinDTOv1> convert(List<TwinEntity> twinEntityList) {
        List<TwinDTOv1> ret = new ArrayList<>();
        for (TwinEntity twinEntity : twinEntityList) {
            ret.add(twinRestDTOMapper.convert(twinEntity));
        }
        return ret;
    }
}
